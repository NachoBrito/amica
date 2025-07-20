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

package es.nachobrito.amica.infrastructure.hivemqtt;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import es.nachobrito.amica.domain.model.message.*;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nacho
 */
public class HiveMqttMessageBus implements MessageBus {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String CONVERSATION_ID = "CONVERSATION_ID";

    private Mqtt5AsyncClient subscriptionClient;
    private final String host;
    private final String identifier;
    private final PayloadSerializer payloadSerializer;

    private static final String CLIENT_IDENTIFIER_PATTERN = "A.M.I.C.A / %s";

    private final Map<ConversationId, Mqtt5Client> conversationClients = new ConcurrentHashMap<>();

    public HiveMqttMessageBus(String host, String identifier, PayloadSerializer payloadSerializer) {
        this.host = host;
        this.identifier = identifier;
        this.payloadSerializer = payloadSerializer;
    }

    @Override
    public void send(Message<?> message) {
        var topic = toTopic(message.topic());

        Mqtt5AsyncClient client = getClient(message);
        var future = publishMessage(client, topic, message);

        future.thenCompose(publishResult -> client.disconnect());
    }

    private Mqtt5AsyncClient getClient(Message<?> message) {
        return Mqtt5Client.builder()
                .identifier(CLIENT_IDENTIFIER_PATTERN.formatted(message.id()))
                .serverHost(host)
                .buildAsync();
    }

    private Mqtt5AsyncClient getSubscriptionClient() {
        if (subscriptionClient == null) {
            connect();
        }
        return subscriptionClient;
    }

    private CompletableFuture<Mqtt5PublishResult> publishMessage(Mqtt5AsyncClient client, String topic, Message<?> message) {
        var payload = payloadSerializer.serialize(message.payload());
        var userProperties = Mqtt5UserProperties.of(
                Mqtt5UserProperty.of(MESSAGE_ID, message.id().value()),
                Mqtt5UserProperty.of(CONVERSATION_ID, message.conversationId().value())
        );
        return client.connect()
                .thenCompose(connAck -> client
                        .publishWith()
                        .topic(topic)
                        .userProperties(userProperties)
                        .payload(payload.getBytes()).send());
    }

    @Override
    public void send(Message<?> message, MessageConsumer<AgentResponse> responseConsumer) {
        var messageTopic = toTopic(message.topic());
        var responsesTopic = toResponsesTopic(message.id());
        var client = getClient(message);
        //1. subscribe for message responses with a ConversationBuffer
        var buffer = new ConversationBuffer(responseConsumer, () -> {
            getSubscriptionClient().unsubscribeWith().topicFilter(responsesTopic).send();
        });
        registerConsumer(new MessageTopic(responsesTopic), AgentResponse.class, response -> {
            logger.debug("Received response: {}", response);
            buffer.accept(response);
        });
        publishMessage(client, messageTopic, message).thenCompose(publishResult -> client.disconnect());
    }

    @Override
    public void respond(MessageId originalMessageId, Message<AgentResponse> response) {
        var topic = toResponsesTopic(originalMessageId);
        var client = getClient(response);
        var future = publishMessage(client, topic, response);
        future.thenAccept(publishResult -> client.disconnect());
    }

    private String toResponsesTopic(MessageId originalMessageId) {
        return "/replies/%s".formatted(originalMessageId.value());
    }


    private String toTopic(MessageTopic topic) {
        if (topic.name().charAt(0) == '/') {
            return topic.name();
        }
        return "/%s".formatted(topic.name().replace('.', '/'));
    }

    private MessageTopic fromTopic(String topic) {
        return new MessageTopic(topic);
    }

    @Override
    public <P extends MessagePayload> void registerConsumer(MessageTopic messageTopic, Class<P> payloadType, MessageConsumer<P> consumer) {
        var topic = toTopic(messageTopic);
        getSubscriptionClient().subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(mqtt5Publish -> {
                    consumer.consume(toMessage(mqtt5Publish, payloadType));
                })
                .send();
    }

    private <P extends MessagePayload> Message<P> toMessage(Mqtt5Publish mqtt5Publish, Class<P> payloadType) {
        String payloadString = StandardCharsets.UTF_8.decode(mqtt5Publish.getPayload().orElseThrow()).toString();
        logger.debug("Deserializing payload to {}: {}", payloadType, payloadString);
        var payload = payloadSerializer.deSerialize(payloadString, payloadType);
        var topic = fromTopic(mqtt5Publish.getTopic().filter().toString());
        var properties = mqtt5Publish.getUserProperties().asList();
        MessageId messageId = null;
        ConversationId conversationId = null;
        for (Mqtt5UserProperty property : properties) {
            switch (property.getName().toString()) {
                case MESSAGE_ID:
                    messageId = new MessageId(property.getValue().toString());
                    break;

                case CONVERSATION_ID:
                    conversationId = new ConversationId(property.getValue().toString());
                    break;
            }
        }
        if (messageId == null) {
            throw new InvalidMessageException("Message ID cannot be null!");
        }
        return new Message<>(messageId, conversationId, topic, payload);
    }


    private void connect() {
        var client = Mqtt5Client.builder()
                .identifier(CLIENT_IDENTIFIER_PATTERN.formatted(identifier))
                .serverHost(host)
                .buildBlocking();
        client.connect();
        subscriptionClient = client.toAsync();
    }
}
