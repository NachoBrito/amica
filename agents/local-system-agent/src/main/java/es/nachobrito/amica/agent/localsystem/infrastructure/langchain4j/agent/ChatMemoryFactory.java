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

package es.nachobrito.amica.agent.localsystem.infrastructure.langchain4j.agent;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import es.nachobrito.amica.domain.model.agent.AgentDetails;
import es.nachobrito.amica.domain.model.agent.conversation.Conversation;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author nacho
 */
public class ChatMemoryFactory {

  /**
   * Creates a ChatMemory wrapper around a given Conversation
   *
   * @param conversation the localsystem instance
   * @param agentDetails the AgentDetails instance with information about the current Agent
   * @return a new ChatMemory that will wrap the Conversation
   */
  public static ChatMemory with(Conversation conversation, AgentDetails agentDetails) {
    return new ChatMemory() {
      @Override
      public Object id() {
        return conversation.getId().value();
      }

      @Override
      public void add(ChatMessage message) {
        switch (message) {
          case UserMessage userMessage:
            conversation.add(
                ConversationMessage.ofUserMessage(userMessage.name(), userMessage.singleText()));
            break;
          case AiMessage aiMessage:
            conversation.add(
                ConversationMessage.ofAgentResponse(
                    agentDetails,
                    aiMessage.text(),
                    aiMessage.thinking(),
                    aiMessage.hasToolExecutionRequests()
                        ? aiMessage.toolExecutionRequests().stream()
                            .map(
                                it ->
                                    new ConversationMessage.ToolExecutionRequest(
                                        it.id(), it.name(), it.arguments()))
                            .collect(Collectors.toSet())
                        : null));
            break;
          case ToolExecutionResultMessage toolExecutionResultMessage:
            conversation.add(
                ConversationMessage.ofToolExecution(
                    new ConversationMessage.ToolExecutionRequest(
                        toolExecutionResultMessage.id(), toolExecutionResultMessage.toolName(), ""),
                    toolExecutionResultMessage.text()));
            break;
          default:
            // we don't expect other types of messages (custom, system)
        }
      }

      @Override
      public List<ChatMessage> messages() {
        return conversation.getMessages().stream().map(ChatMessageFactory::with).toList();
      }

      @Override
      public void clear() {}
    };
  }
}
