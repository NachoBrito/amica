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

package es.nachobrito.amica.domain.model.message;

import es.nachobrito.amica.domain.model.message.payload.AgentResponse;

/**
 * @author nacho
 */
public record Message<P extends MessagePayload>(
        MessageId id,
        ConversationId conversationId,
        MessageTopic topic,
        P payload
) {
    /**
     * Creates a new message with the given payload, in a particular topic.
     *
     * @param topic   the topic to link this message to
     * @param payload the message payload
     * @param <P>     the type of the payload
     * @return the new message
     */
    public static <P extends MessagePayload> Message<P> of(MessageTopic topic, P payload) {
        return new Message<>(MessageId.newRandom(), ConversationId.newRandom(), topic, payload);
    }

    /**
     * Creates a new message in the MessageTopic.USER_REQUESTS topic.
     *
     * @param payload the message payload
     * @param <P>     the type of the payload
     * @return the new message
     */
    public static <P extends MessagePayload> Message<P> userRequest(P payload) {
        return new Message<>(MessageId.newRandom(), ConversationId.newRandom(), MessageTopic.USER_REQUESTS, payload);
    }

    /**
     * Creates a new message that is a system event.
     *
     * @param event the event payload
     * @param <E>   the type of event payload
     * @return the new event message
     */
    public static <E extends SystemEvent> Message<E> systemEvent(E event) {
        return new Message<>(MessageId.newRandom(), ConversationId.newRandom(), MessageTopic.SYSTEM_EVENTS, event);
    }

    /**
     * Creates a new message that is an LLM response to a user message.
     *
     * @param original        the original user message
     * @param responsePayload the LLM response payload
     * @return the new message
     */
    public static Message<AgentResponse> responseTo(Message<?> original, AgentResponse responsePayload) {
        return new Message<>(MessageId.newRandom(), original.conversationId(), original.topic(), responsePayload);
    }
}
