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
import es.nachobrito.amica.domain.model.message.payload.ConversationEnded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nacho
 */
public class HiveMqttMessageBus implements MessageBus {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    static final String MESSAGE_ID = "MESSAGE_ID";
    static final String CONVERSATION_ID = "CONVERSATION_ID";
    static final String PAYLOAD_TYPE = "PAYLOAD_TYPE";
    final Map<MessageTopic, Set<TopicHandler<?>>> topicHandlers = new HashMap<>();

    private Mqtt5AsyncClient subscriptionClient;
    private final String host;
    private final String identifier;
    private final PayloadSerializer payloadSerializer;

    private static final String CLIENT_IDENTIFIER_PATTERN = "A.M.I.C.A. / %s";

    public HiveMqttMessageBus(String host, String identifier, PayloadSerializer payloadSerializer) {
        this.host = host;
        this.identifier = identifier;
        this.payloadSerializer = payloadSerializer;
    }

    @Override
    public void send(Message<?> message) {
        var topic = toMqttTopic(message.topic());

        Mqtt5AsyncClient client = getClient(message);
        var future = publishMessage(client, topic, message);

        future.thenCompose(publishResult -> client.disconnect());
    }

    @Override
    public void send(Message<?> message, MessageConsumer<AgentResponse> responseConsumer) {
        var mqttMessageTopic = toMqttTopic(message.topic());
        var mqttResponsesTopic = toMqttResponsesTopic(message.id());
        var responsesTopic = MessageFactory.fromTopic(mqttResponsesTopic);
        var client = getClient(message);

        //1. subscribe for message responses with a ConversationBuffer
        var buffer = new ConversationBuffer(responseConsumer, () -> {
            unRegisterConsumer(responsesTopic, AgentResponse.class);
        });
        registerConsumer(responsesTopic, AgentResponse.class, buffer::accept);
        publishMessage(client, mqttMessageTopic, message).thenCompose(publishResult -> client.disconnect());
    }

    @Override
    public void respond(MessageId originalMessageId, Message<AgentResponse> response) {
        var topic = toMqttResponsesTopic(originalMessageId);
        var client = getClient(response);
        var future = publishMessage(client, topic, response);
        future.thenAccept(publishResult -> client.disconnect());

        if (Boolean.TRUE.equals(response.payload().isComplete())) {
            send(Message.systemEvent(new ConversationEnded(response.conversationId().value())));
        }
    }

    @Override
    public <P extends MessagePayload> void registerConsumer(MessageTopic messageTopic, Class<P> payloadType, MessageConsumer<P> consumer) {
        getTopicHandlers(messageTopic)
                .add(new TopicHandler<>(payloadType, consumer));
    }

    public <P extends MessagePayload> void unRegisterConsumer(MessageTopic messageTopic, Class<P> payloadType) {
        var handlers = getTopicHandlers(messageTopic);
        handlers.removeIf(it -> it.payloadType().equals(payloadType));
        if (handlers.isEmpty()) {
            getSubscriptionClient().unsubscribeWith().topicFilter(messageTopic.name()).send();
        }
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
                Mqtt5UserProperty.of(CONVERSATION_ID, message.conversationId().value()),
                Mqtt5UserProperty.of(PAYLOAD_TYPE, message.payload().getClass().getName())
        );
        return client.connect()
                .thenCompose(connAck -> client
                        .publishWith()
                        .topic(topic)
                        .userProperties(userProperties)
                        .payload(payload.getBytes()).send());
    }

    private String toMqttResponsesTopic(MessageId originalMessageId) {
        return "/es/nachobrito/amica/replies/%s".formatted(originalMessageId.value());
    }

    private String toMqttTopic(MessageTopic topic) {
        if (topic.name().charAt(0) == '/') {
            return topic.name();
        }
        return "/%s".formatted(topic.name().replace('.', '/'));
    }


    private Set<TopicHandler<?>> getTopicHandlers(MessageTopic messageTopic) {
        if (topicHandlers.containsKey(messageTopic)) {
            return topicHandlers.get(messageTopic);
        }
        topicHandlers.computeIfAbsent(messageTopic, k -> ConcurrentHashMap.newKeySet());
        var topic = toMqttTopic(messageTopic);
        getSubscriptionClient().subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(this::handleMessage)
                .send();
        return topicHandlers.get(messageTopic);
    }

    private void handleMessage(Mqtt5Publish mqtt5Publish) {
        var messageTopic = MessageFactory.fromTopic(mqtt5Publish.getTopic().filter().toString());
        var message = MessageFactory.from(mqtt5Publish, payloadSerializer);
        topicHandlers
                .get(messageTopic)
                .stream()
                .filter(it -> it.payloadType().equals(message.payload().getClass()))
                .forEach(it -> it.consume(message));
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
