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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import es.nachobrito.amica.agent.conversation.domain.model.agent.DefaultToolManager;
import es.nachobrito.amica.agent.conversation.infrastructure.InMemoryMessageBus;
import es.nachobrito.amica.domain.model.agent.Memory;
import es.nachobrito.amica.domain.model.agent.ToolManager;
import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.MessageTopic;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nacho
 */
class LangChain4jAgentTest {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final InMemoryMessageBus messageBus = new InMemoryMessageBus();
  private final ToolManager toolManager = new DefaultToolManager();
  private final Memory memory = mock();
  private final ResourceLoader resourceLoader =
      ClassPathResourceLoader.defaultLoader(getClass().getClassLoader());

  private final LangChain4jAgent agent =
      new LangChain4jAgent(messageBus, toolManager, memory, resourceLoader);

  @Test
  void shouldAnswerMessages() {
    final var responses = new ArrayList<Message<AgentResponse>>();
    var message =
        Message.userRequest(new UserRequest(ZonedDateTime.now(), "Nacho", "What can you do?"));

    messageBus.registerConsumer(
        messageBus.getResponsesTopic(message.id()),
        AgentResponse.class,
        it -> {
          responses.add(it);
          System.out.print(it.payload().message());
        });

    messageBus.registerConsumer(
        MessageTopic.USER_REQUESTS, UserRequest.class, agent::onUserMessage);

    messageBus.send(message);

    assertFalse(responses.isEmpty());
  }
}
