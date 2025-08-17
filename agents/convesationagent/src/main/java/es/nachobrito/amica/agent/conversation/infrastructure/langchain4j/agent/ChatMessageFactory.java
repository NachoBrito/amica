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

package es.nachobrito.amica.agent.conversation.infrastructure.langchain4j.agent;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import java.util.List;

/**
 * @author nacho
 */
public class ChatMessageFactory {

  /**
   * Creates the corresponding ChatMessage type depending on the conversation message type.
   *
   * @param conversationMessage the conversation message
   * @return a ChatMessage of the correct type
   */
  static ChatMessage with(ConversationMessage conversationMessage) {
    return switch (conversationMessage.messageType()) {
      case USER_MESSAGE ->
          UserMessage.from(conversationMessage.source(), conversationMessage.text());
      case AGENT_MESSAGE ->
          AiMessage.builder()
              .text(conversationMessage.text())
              .thinking(conversationMessage.agentThinking())
              .toolExecutionRequests(
                  conversationMessage.executionRequests() != null
                      ? conversationMessage.executionRequests().stream()
                          .map(
                              it ->
                                  ToolExecutionRequest.builder()
                                      .id(it.id())
                                      .name(it.toolName())
                                      .arguments(it.toolArguments())
                                      .build())
                          .toList()
                      : List.of())
              .build();
      case TOOL_EXECUTION ->
          ToolExecutionResultMessage.from(
              ToolExecutionRequest.builder()
                  .id(
                      conversationMessage.executionRequests().stream()
                          .findFirst()
                          .orElseThrow()
                          .id())
                  .name(
                      conversationMessage.executionRequests().stream()
                          .findFirst()
                          .orElseThrow()
                          .toolName())
                  .build(),
              conversationMessage.text());
    };
  }
}
