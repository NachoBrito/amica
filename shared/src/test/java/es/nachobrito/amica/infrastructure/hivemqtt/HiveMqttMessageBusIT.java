package es.nachobrito.amica.infrastructure.hivemqtt;

import es.nachobrito.amica.domain.model.message.*;
import es.nachobrito.amica.infrastructure.JacksonPayloadSerializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author nacho
 */
class HiveMqttMessageBusIT {

    Message<PersonPayload> receivedMessage = null;


    @Test
    void integrationTest() {
        assertNull(receivedMessage);

        var topic = new MessageTopic("test-topic");
        var bus = new HiveMqttMessageBus("localhost", "test-bus", new JacksonPayloadSerializer());
        var payload = new PersonPayload("John", "Doe");

        bus.registerConsumer(topic, PersonPayload.class, message -> {
            receivedMessage = message;
        });

        var sentMessage = new Message<>(new MessageId("test-id"), new ConversationId("conversation-id"), topic, payload);
        bus.send(sentMessage);

        await().atMost(Duration.ofSeconds(10)).until(() -> receivedMessage != null);

        assertEquals(sentMessage, receivedMessage);

    }

    record PersonPayload(String name, String surname) implements MessagePayload {

    }

}