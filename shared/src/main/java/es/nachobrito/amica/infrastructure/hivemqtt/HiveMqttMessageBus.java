package es.nachobrito.amica.infrastructure.hivemqtt;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import es.nachobrito.amica.domain.model.message.*;

import java.nio.charset.StandardCharsets;

/**
 * @author nacho
 */
public class HiveMqttMessageBus implements MessageBus {
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String CONVERSATION_ID = "CONVERSATION_ID";

    private Mqtt5BlockingClient subscriptionClient;
    private final String host;
    private final String identifier;
    private final PayloadSerializer payloadSerializer;

    private static final String CLIENT_IDENTIFIER_PATTERN = "A.M.I.C.A /  %s";

    public HiveMqttMessageBus(String host, String identifier, PayloadSerializer payloadSerializer) {
        this.host = host;
        this.identifier = identifier;
        this.payloadSerializer = payloadSerializer;
    }

    @Override
    public void send(Message<?> message) {
        Mqtt5AsyncClient client = Mqtt5Client.builder()
                .identifier(CLIENT_IDENTIFIER_PATTERN.formatted(message.id()))
                .serverHost(host)
                .buildAsync();

        var topic = toTopic(message.topic());
        var payload = payloadSerializer.serialize(message.payload());
        var userProperties = Mqtt5UserProperties.of(
                Mqtt5UserProperty.of(MESSAGE_ID, message.id().value()),
                Mqtt5UserProperty.of(CONVERSATION_ID, message.conversationId().value())
        );
        client.connect()
                .thenCompose(connAck -> client
                        .publishWith()
                        .topic(topic)
                        .userProperties(userProperties)
                        .payload(payload.getBytes()).send())
                .thenCompose(publishResult -> client.disconnect());
    }

    private String toTopic(MessageTopic topic) {
        return topic.name();
    }

    private MessageTopic fromTopic(String topic) {
        return new MessageTopic(topic);
    }

    @Override
    public <P extends MessagePayload> void registerConsumer(MessageTopic messageTopic, Class<P> payloadType, MessageConsumer<P> consumer) {
        if (subscriptionClient == null) {
            connect();
        }
        var topic = toTopic(messageTopic);

        subscriptionClient.toAsync().subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(mqtt5Publish -> {
                    consumer.consume(toMessage(mqtt5Publish, payloadType));
                })
                .send();
    }

    private <P extends MessagePayload> Message<P> toMessage(Mqtt5Publish mqtt5Publish, Class<P> payloadType) {
        String payloadString = StandardCharsets.UTF_8.decode(mqtt5Publish.getPayload().orElseThrow()).toString();

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
        if (conversationId == null) {
            throw new InvalidMessageException("Conversation ID cannot be null!");
        }
        return new Message<>(messageId, conversationId, topic, payload);
    }


    private void connect() {
        subscriptionClient = Mqtt5Client.builder()
                .identifier(CLIENT_IDENTIFIER_PATTERN.formatted(identifier))
                .serverHost(host)
                .buildBlocking();
        subscriptionClient.connect();
    }
}
