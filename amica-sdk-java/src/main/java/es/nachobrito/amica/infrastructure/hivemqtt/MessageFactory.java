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

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import es.nachobrito.amica.domain.model.message.*;

import java.nio.charset.StandardCharsets;

import static es.nachobrito.amica.infrastructure.hivemqtt.HiveMqttMessageBus.*;

/**
 * @author nacho
 */
public class MessageFactory {

    @SuppressWarnings("unchecked")
    public static <P extends MessagePayload> Message<? extends MessagePayload> from(Mqtt5Publish mqtt5Publish, PayloadSerializer payloadSerializer) {
        var properties = mqtt5Publish.getUserProperties().asList();
        MessageId messageId = null;
        ConversationId conversationId = null;
        Class<P> payloadType = null;
        for (Mqtt5UserProperty property : properties) {
            switch (property.getName().toString()) {
                case MESSAGE_ID:
                    messageId = new MessageId(property.getValue().toString());
                    break;

                case CONVERSATION_ID:
                    conversationId = new ConversationId(property.getValue().toString());
                    break;

                case PAYLOAD_TYPE:
                    try {
                        payloadType = (Class<P>) Class.forName(property.getValue().toString());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
            }
        }
        if (messageId == null) {
            throw new InvalidMessageException("Message ID cannot be null!");
        }
        if (payloadType == null) {
            throw new InvalidMessageException("Payload type cannot be null!");
        }

        String payloadString = StandardCharsets.UTF_8.decode(mqtt5Publish.getPayload().orElseThrow()).toString();
        var payload = payloadSerializer.deSerialize(payloadString, payloadType);
        var topic = fromTopic(mqtt5Publish.getTopic().filter().toString());

        return new Message<>(messageId, conversationId, topic, payload);
    }

    static MessageTopic fromTopic(String topic) {
        // /the/topic -> the.topic
        if ('/' == topic.charAt(0)) {
            topic = topic.substring(1).replace('/', '.');
        }
        return new MessageTopic(topic);
    }
}
