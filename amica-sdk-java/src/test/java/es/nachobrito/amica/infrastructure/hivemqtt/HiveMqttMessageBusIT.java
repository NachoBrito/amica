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

import es.nachobrito.amica.domain.model.message.*;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import es.nachobrito.amica.domain.model.message.payload.SequenceNumber;
import es.nachobrito.amica.infrastructure.JacksonPayloadSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author nacho
 */
class HiveMqttMessageBusIT {

    Message<PersonPayload> receivedPerson = null;
    Message<ThingPayload> receivedThing = null;

    @Test
    void integrationTest() {
        assertNull(receivedPerson);

        var topic = new MessageTopic("test-topic");
        var bus = new HiveMqttMessageBus("localhost", "test-bus", new JacksonPayloadSerializer());
        var personPayload = new PersonPayload("John", "Doe");
        var thingPayload = new ThingPayload("White table");

        bus.registerConsumer(topic, PersonPayload.class, message -> {
            receivedPerson = message;
        });

        bus.registerConsumer(topic, ThingPayload.class, message -> {
            receivedThing = message;
        });

        var personMessage = new Message<>(new MessageId("test-id-1"), new ConversationId("conversation-id"), topic, personPayload);
        var thingMessage = new Message<>(new MessageId("test-id-2"), new ConversationId("conversation-id"), topic, thingPayload);
        bus.send(personMessage, thingMessage);

        await().atMost(Duration.ofSeconds(10)).until(() -> receivedPerson != null && receivedThing != null);

        assertEquals(personMessage, receivedPerson);
    }

    @DisplayName("When responses to a message are published out of order, the buffer sorts them correctly.")
    @Test
    void conversationSortingTest() {
        var receivedMessages = new ArrayList<AgentResponse>();

        var request = Message.userRequest(new PersonPayload("John", "Doe"));
        var bus = new HiveMqttMessageBus("localhost", "test-bus", new JacksonPayloadSerializer());
        bus.send(request, msg -> {
            receivedMessages.add(msg.payload());
        });

        var agentResponse1 = new AgentResponse("tokens", ZonedDateTime.now(ZoneOffset.UTC), false, new SequenceNumber(1));
        var agentResponse2 = new AgentResponse("tokens", ZonedDateTime.now(ZoneOffset.UTC), true, new SequenceNumber(2));

        bus.respond(request.id(), Message.responseTo(request, agentResponse2));
        await().during(Duration.ofMillis(500));
        bus.respond(request.id(), Message.responseTo(request, agentResponse1));

        await().atMost(Duration.ofSeconds(30)).until(() -> receivedMessages.size() == 2);
        assertEquals(agentResponse1, receivedMessages.get(0));
        assertEquals(agentResponse2, receivedMessages.get(1));
    }

    record PersonPayload(String name, String surname) implements MessagePayload {
    }

    record ThingPayload(String description) implements MessagePayload {
    }


}