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

package es.nachobrito.amica.agent.conversation.infrastructure;

import es.nachobrito.amica.domain.model.message.*;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nacho
 */
public class InMemoryMessageBus implements MessageBus {

  private final Map<MessageTopic, List<Message<?>>> messages = new HashMap<>();
  private final Map<MessageTopic, List<MessageConsumer<?>>> consumersByTopic = new HashMap<>();
  private final Map<Class<? extends MessagePayload>, List<MessageConsumer<?>>>
      consumersByPayloadType = new HashMap<>();

  @Override
  public void send(Message<?> message) {
    messages.computeIfAbsent(message.topic(), it -> new ArrayList<>()).add(message);
    invokeConsumers(message);
  }

  @SuppressWarnings("unchecked")
  private <P extends MessagePayload> List<MessageConsumer<P>> getPayloadConsumers(
      Class<P> payloadType) {
    return consumersByPayloadType.computeIfAbsent(payloadType, _ -> new ArrayList<>()).stream()
        .map(it -> (MessageConsumer<P>) it)
        .toList();
  }

  @SuppressWarnings("unchecked")
  private <P extends MessagePayload> void invokeConsumers(Message<P> message) {
    var payloadConsumers = getPayloadConsumers(message.payload().getClass());
    consumersByTopic.computeIfAbsent(message.topic(), _ -> new ArrayList<>()).stream()
        .filter(payloadConsumers::contains)
        .map(it -> (MessageConsumer<P>) it)
        .forEach(
            it -> {
              it.consume(message);
            });
  }

  public MessageTopic getResponsesTopic(MessageId messageId) {
    return new MessageTopic("responses/" + messageId.value());
  }

  @Override
  public void send(Message<?> message, MessageConsumer<AgentResponse> messageConsumer) {
    registerConsumer(getResponsesTopic(message.id()), AgentResponse.class, messageConsumer);
    send(message);
  }

  @Override
  public void respond(MessageId messageId, Message<AgentResponse> message) {
    var responseTopic = getResponsesTopic(messageId);
    var newMessage =
        new Message(message.id(), message.conversationId(), responseTopic, message.payload());
    send(newMessage);
  }

  @Override
  public <P extends MessagePayload> void registerConsumer(
      MessageTopic messageTopic, Class<P> payloadType, MessageConsumer<P> messageConsumer) {
    consumersByTopic.computeIfAbsent(messageTopic, _ -> new ArrayList<>()).add(messageConsumer);

    consumersByPayloadType
        .computeIfAbsent(payloadType, _ -> new ArrayList<>())
        .add(messageConsumer);
  }
}
