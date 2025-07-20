/*
 *    Copyright 2025 Nacho Brito
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package es.nachobrito.amica.infrastructure.valkey;

import es.nachobrito.amica.domain.model.message.*;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import io.valkey.JedisPool;
import io.valkey.JedisPubSub;

/**
 * @author nacho
 */
public class ValkeyMessageBus implements MessageBus {
    private JedisPool jedisPool = null;
    private static final String SEPARATOR = "@@";

    private final String host;
    private final int port;
    private final PayloadSerializer payloadSerializer;

    public ValkeyMessageBus(String host, int port, PayloadSerializer payloadSerializer) {
        this.host = host;
        this.port = port;
        this.payloadSerializer = payloadSerializer;
    }


    private JedisPool getJedisPool() {
        if (jedisPool == null) {
            io.valkey.JedisPoolConfig config = new io.valkey.JedisPoolConfig();
            config.setMaxTotal(2);
            config.setMaxIdle(2);
            config.setMinIdle(1);
            jedisPool = new io.valkey.JedisPool(config, host, port);
        }
        return jedisPool;
    }

    @Override
    public void send(Message<?> message) {
        try (io.valkey.Jedis jedis = getJedisPool().getResource()) {
            var channel = getChannel(message.topic());
            jedis.publish(channel, serializeMessage(message));
        } catch (Exception e) {
            throw new ValkeyException("Could not publish message: %s".formatted(message), e);
        }
    }

    private String getChannel(MessageTopic topic) {
        return topic.name();
    }

    @Override
    public void send(Message<?> message, MessageConsumer<AgentResponse> responseConsumer) {
//TODO
    }

    @Override
    public void respond(MessageId originalMessageId, Message<AgentResponse> response) {
        //todo
    }

    @Override
    public <P extends MessagePayload> void registerConsumer(MessageTopic messageTopic, Class<P> payloadType, MessageConsumer<P> consumer) {
        try (io.valkey.Jedis jedis = getJedisPool().getResource()) {
            var subscriber = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String serializedMessage) {
                    //noinspection unchecked
                    var message = (Message<P>) deserializeMessage(serializedMessage);
                    consumer.consume(message);
                }
            };
            jedis.subscribe(subscriber, getChannel(messageTopic));
        } catch (Exception e) {
            throw new ValkeyException("Could not subscribe to topic: %s".formatted(messageTopic.name()), e);
        }
    }


    private String serializeMessage(Message<?> message) {
        return String.join(SEPARATOR,
                message.id().value(),
                message.conversationId().value(),
                message.topic().name(),
                message.payload().getClass().getName(),
                payloadSerializer.serialize(message.payload()));
    }

    private Message<?> deserializeMessage(String serialized) {
        var parts = serialized.split(SEPARATOR);
        if (parts.length != 5) {
            throw new ValkeyException("Invalid message! %s".formatted(serialized));
        }

        try {
            var messageId = new MessageId(parts[0]);
            var conversationId = new ConversationId(parts[1]);
            var topic = new MessageTopic(parts[2]);
            //noinspection unchecked
            var payloadClass = (Class<? extends MessagePayload>) Class.forName(parts[3]);
            var payload = payloadSerializer.deSerialize(parts[4], payloadClass);
            return new Message<>(messageId, conversationId, topic, payload);
        } catch (ClassNotFoundException e) {
            throw new ValkeyException("Payload class not found!", e);
        }

    }
}
