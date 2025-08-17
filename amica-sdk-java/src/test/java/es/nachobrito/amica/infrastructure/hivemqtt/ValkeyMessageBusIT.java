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
import es.nachobrito.amica.infrastructure.JacksonPayloadSerializer;
import es.nachobrito.amica.infrastructure.valkey.ValkeyMessageBus;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author nacho
 */
class ValkeyMessageBusIT {

    Message<PersonPayload> receivedMessage = null;


    @Test
    void integrationTest() {
        assertNull(receivedMessage);

        var topic = new MessageTopic("test-topic");
        var bus = new ValkeyMessageBus("localhost", 6379, new JacksonPayloadSerializer());
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