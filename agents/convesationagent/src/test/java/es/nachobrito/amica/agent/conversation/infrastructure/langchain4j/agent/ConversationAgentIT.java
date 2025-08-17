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

import static org.junit.jupiter.api.Assertions.*;

import es.nachobrito.amica.agent.conversation.domain.model.agent.tool.*;
import es.nachobrito.amica.agent.conversation.infrastructure.InMemoryMessageBus;
import es.nachobrito.amica.agent.conversation.infrastructure.valkey.SerdeMessageSerializer;
import es.nachobrito.amica.domain.model.agent.Memory;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessageType;
import es.nachobrito.amica.domain.model.agent.conversation.MessageSerializer;
import es.nachobrito.amica.domain.model.agent.tool.DefaultToolExecutor;
import es.nachobrito.amica.domain.model.agent.tool.ToolExecutor;
import es.nachobrito.amica.domain.model.agent.tool.ToolManager;
import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import es.nachobrito.amica.infrastructure.lucene.LuceneToolManager;
import es.nachobrito.amica.infrastructure.valkey.ValkeyMemory;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.serde.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nacho
 */
class ConversationAgentIT {
  private static final String VALKEY_HOST = "localhost";
  private static final int VALKEY_PORT = 6379;
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final InMemoryMessageBus messageBus = new InMemoryMessageBus();
  private final Memory memory;

  ConversationAgentIT() {
    ToolManager toolManager =
        new LuceneToolManager(
            Set.of(new UptimeTool(), new LoadAverage5Tool(), new UserCountTool()));
    ToolExecutor toolExecutor = new DefaultToolExecutor();
    MessageSerializer messageSerializer = new SerdeMessageSerializer(ObjectMapper.getDefault());
    memory = new ValkeyMemory(messageSerializer, VALKEY_HOST, VALKEY_PORT);
    ResourceLoader resourceLoader =
        ClassPathResourceLoader.defaultLoader(getClass().getClassLoader());
    ConversationAgent agent = new ConversationAgent(messageBus, toolManager, toolExecutor, memory);
    agent.register(messageBus);
  }

  @Test
  void shouldAnswerMessagesUsingTools() {
    final var responses = new ArrayList<Message<AgentResponse>>();
    var message =
        Message.userRequest(
            new UserRequest(
                ZonedDateTime.now(),
                "Nacho",
                "What was the load average of this system in the last 5 minutes?"));
    var expectedToolName = new LoadAverage5Tool().getName();
    messageBus.registerConsumer(
        messageBus.getResponsesTopic(message.id()), AgentResponse.class, responses::add);

    messageBus.send(message);
    assertEquals(1, responses.size());
    var response = responses.getFirst();
    var messages = memory.getConversation(response.conversationId()).getMessages();

    var usedRightTool = false;
    var correctResult = false;
    var executedExtraTools = false;

    for (ConversationMessage msg : messages) {
      if (!usedRightTool && msg.messageType() == ConversationMessageType.AGENT_MESSAGE) {
        usedRightTool =
            msg.executionRequests() != null
                && msg.executionRequests().size() == 1
                && msg.executionRequests().stream()
                    .anyMatch(it -> it.toolName().equals(expectedToolName));
      }

      if (!correctResult && msg.messageType() == ConversationMessageType.TOOL_EXECUTION) {
        if (msg.source().equals(expectedToolName)) {
          correctResult = response.payload().message().contains(msg.text());
        } else {
          executedExtraTools = true;
        }
      }
    }
    assertTrue(usedRightTool);
    assertTrue(correctResult);
    assertFalse(executedExtraTools);
  }
}
