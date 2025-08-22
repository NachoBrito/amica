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

package es.nachobrito.amica.agent.minutetaker.infrastructure.langchain4j.agent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessageType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChatMessageFactoryTest {

  @Test
  void shouldCreateUserMessage() {
    ConversationMessage conversationMessage = mock(ConversationMessage.class);
    when(conversationMessage.messageType()).thenReturn(ConversationMessageType.USER_MESSAGE);
    when(conversationMessage.source()).thenReturn("nacho");
    when(conversationMessage.text()).thenReturn("hello world");

    ChatMessage result = ChatMessageFactory.with(conversationMessage);

    assertInstanceOf(UserMessage.class, result);
    UserMessage userMessage = (UserMessage) result;
    assertEquals("nacho", userMessage.name());
    assertEquals("hello world", userMessage.singleText());
  }

  @Test
  void shouldCreateAiMessageWithoutExecutionRequests() {
    ConversationMessage conversationMessage = mock(ConversationMessage.class);
    when(conversationMessage.messageType()).thenReturn(ConversationMessageType.AGENT_MESSAGE);
    when(conversationMessage.text()).thenReturn("answer");
    when(conversationMessage.agentThinking()).thenReturn("thinking...");
    when(conversationMessage.executionRequests()).thenReturn(null);

    ChatMessage result = ChatMessageFactory.with(conversationMessage);

    assertInstanceOf(AiMessage.class, result);
    AiMessage aiMessage = (AiMessage) result;
    assertEquals("answer", aiMessage.text());
    assertEquals("thinking...", aiMessage.thinking());
    assertTrue(aiMessage.toolExecutionRequests().isEmpty());
  }

  @Test
  void shouldCreateAiMessageWithExecutionRequests() {
    ConversationMessage conversationMessage = mock(ConversationMessage.class);
    when(conversationMessage.messageType()).thenReturn(ConversationMessageType.AGENT_MESSAGE);
    when(conversationMessage.text()).thenReturn("agent response");
    when(conversationMessage.agentThinking()).thenReturn("deep thoughts");

    ConversationMessage.ToolExecutionRequest execReq =
        mock(ConversationMessage.ToolExecutionRequest.class);
    when(execReq.id()).thenReturn("123");
    when(execReq.toolName()).thenReturn("myTool");
    when(execReq.toolArguments()).thenReturn("{\"param\":42}");
    when(conversationMessage.executionRequests()).thenReturn(Set.of(execReq));

    ChatMessage result = ChatMessageFactory.with(conversationMessage);

    assertInstanceOf(AiMessage.class, result);
    AiMessage aiMessage = (AiMessage) result;
    assertEquals("agent response", aiMessage.text());
    assertEquals("deep thoughts", aiMessage.thinking());
    assertEquals(1, aiMessage.toolExecutionRequests().size());

    ToolExecutionRequest mappedRequest = aiMessage.toolExecutionRequests().get(0);
    assertEquals("123", mappedRequest.id());
    assertEquals("myTool", mappedRequest.name());
    assertEquals("{\"param\":42}", mappedRequest.arguments());
  }

  @Test
  void shouldCreateAiMessageWithNullValues() {
    ConversationMessage conversationMessage = mock(ConversationMessage.class);
    when(conversationMessage.messageType()).thenReturn(ConversationMessageType.AGENT_MESSAGE);
    when(conversationMessage.text()).thenReturn(null);
    when(conversationMessage.agentThinking()).thenReturn(null);
    when(conversationMessage.executionRequests()).thenReturn(null);

    ChatMessage result = ChatMessageFactory.with(conversationMessage);

    AiMessage aiMessage = (AiMessage) result;
    assertNull(aiMessage.text()); // null → empty string
    assertNull(aiMessage.thinking()); // null → empty string
    assertTrue(aiMessage.toolExecutionRequests().isEmpty());
  }

  @Test
  void shouldCreateToolExecutionResultMessage() {
    ConversationMessage conversationMessage = mock(ConversationMessage.class);
    when(conversationMessage.messageType()).thenReturn(ConversationMessageType.TOOL_EXECUTION);
    when(conversationMessage.source()).thenReturn("calculator");
    when(conversationMessage.text()).thenReturn("42");
    when(conversationMessage.executionRequests())
        .thenReturn(Set.of(new ConversationMessage.ToolExecutionRequest("1", "calculator", "42")));

    ChatMessage result = ChatMessageFactory.with(conversationMessage);

    assertInstanceOf(ToolExecutionResultMessage.class, result);
    ToolExecutionResultMessage toolMessage = (ToolExecutionResultMessage) result;

    assertEquals("42", toolMessage.text());
    assertEquals("calculator", toolMessage.toolName());
  }
}
