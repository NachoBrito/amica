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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.langchain4j.internal.JsonParsingUtils;
import es.nachobrito.amica.agent.minutetaker.infrastructure.InMemoryMessageBus;
import es.nachobrito.amica.domain.model.agent.AgentDetails;
import es.nachobrito.amica.domain.model.agent.Memory;
import es.nachobrito.amica.domain.model.agent.conversation.Conversation;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import es.nachobrito.amica.domain.model.message.ConversationId;
import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.payload.ConversationEnded;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author nacho
 */
class MinuteTakerAgentTest {
  private final InMemoryMessageBus messageBus = new InMemoryMessageBus();
  private final Memory memory;
  private final MinuteTakerAgent agent;

  MinuteTakerAgentTest() {
    memory = mock(Memory.class);
    agent = new MinuteTakerAgent(memory);
    agent.register(messageBus);
  }

  @Test
  void shouldAnswerMessagesUsingTools() throws IOException {
    var conversationId = ConversationId.newRandom();
    var messages =
        List.of(
            ConversationMessage.ofUserMessage("Nacho", "What is the weather like in Madrid today?"),
            ConversationMessage.ofAgentResponse(
                new AgentDetails("weather-agent", "Weather Agent"),
                "Today is cloudy in Madrid, temperature is 16C",
                null,
                Set.of()));
    var conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn(conversationId);
    when(conversation.getMessages()).thenReturn(messages);
    when(memory.getConversation(conversationId)).thenReturn(conversation);

    agent.onSystemMessage(Message.systemEvent(new ConversationEnded(conversationId.value())));

    final var file = Path.of("./data/%s.minute.json".formatted(conversationId.value()));
    await().atMost(Duration.ofSeconds(60)).until(() -> file.toFile().isFile());

    var json = Files.readString(file);
    assertFalse(json.isEmpty());

    var minute = JsonParsingUtils.extractAndParseJson(json, Minute.class).orElseThrow().value();
    var actors = minute.actors();
    Arrays.sort(actors);
    assertArrayEquals(new String[] {"Nacho", "weather-agent"}, actors);
    assertEquals(2, minute.messages().length);
    assertFalse(minute.summary().isEmpty());
  }
}
