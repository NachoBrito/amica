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

import java.util.Arrays;


/// Generic Message Bus interface that can be used to:
///
/// - Publish [Message] instances
/// - Maintain conversations, where several [Message] instances are published in response to a first one.
///
/// Do not confuse value with response messages. value is a way to keep multiple messages (and their
/// responses) linked in a same context. MessageBus implementations should verify that responses to a message contain
/// the same value that the original one.
public interface MessageBus {
    /**
     * Publishes a message to the bus.
     *
     * @param message the message
     */
    void send(Message<?> message);

    default void send(Message<?>... messages) {
        Arrays.stream(messages).forEach(this::send);
    }

    /**
     * Publishes a message to the bus, expecting other messages from the LLM in response.
     *
     * @param message          the message
     * @param responseConsumer a function to consume LLM response messages
     */
    void send(Message<?> message, MessageConsumer<AgentResponse> responseConsumer);

    /**
     * Publish a response from the LLM. This method should implement the required logic so that this response is
     * routed to the consumer function provided in the send(message, consumer) method.
     *
     * @param originalMessageId identifies the message we are responding to
     * @param response          the new message
     */
    void respond(MessageId originalMessageId, Message<AgentResponse> response);

    /**
     * Registers a consumer for messages with a given type of payload
     *
     * @param messageTopic the topic to subscribe to
     * @param payloadType  the expected payload type
     * @param consumer     the consumer object
     * @param <P>          payload type parameter
     */
    <P extends MessagePayload> void registerConsumer(MessageTopic messageTopic, Class<P> payloadType, MessageConsumer<P> consumer);
}
