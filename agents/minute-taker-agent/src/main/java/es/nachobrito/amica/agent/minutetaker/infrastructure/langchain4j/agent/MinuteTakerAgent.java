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

import static es.nachobrito.amica.agent.minutetaker.infrastructure.langchain4j.agent.AIAssistantFactory.CHAT_MODEL;

import es.nachobrito.amica.domain.model.agent.Agent;
import es.nachobrito.amica.domain.model.agent.AgentDetails;
import es.nachobrito.amica.domain.model.agent.Memory;
import es.nachobrito.amica.domain.model.agent.conversation.Conversation;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessageType;
import es.nachobrito.amica.domain.model.message.*;
import es.nachobrito.amica.domain.model.message.payload.ConversationEnded;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nacho
 */
@Singleton
public class MinuteTakerAgent implements Agent {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final AgentDetails agentDetails =
      new AgentDetails(
          "minutetaker-agent", "Conversation Agent (Langchain4J/JLama/%s)".formatted(CHAT_MODEL));

  private final Memory memory;

  private AIAssistant assistant;

  public MinuteTakerAgent( Memory memory) {

    this.memory = memory;
  }

  private AIAssistant getAssistant() {
    if (assistant == null) {
      assistant = AIAssistantFactory.create();
    }
    return assistant;
  }

  @Override
  public AgentDetails getDetails() {
    return agentDetails;
  }

  @Override
  public void onUserMessage(Message<UserRequest> userMessage) {
    // This agent does not respond to user requests.
  }

  @Override
  public void onSystemMessage(Message<? extends SystemEvent> message) {
    if (message.payload() instanceof ConversationEnded conversationEnded) {
      onConversationEnded(conversationEnded);
    }
  }

  private void onConversationEnded(ConversationEnded conversationEnded) {
    var conversation =
        memory.getConversation(new ConversationId(conversationEnded.conversationId()));
    if (conversation.getMessages().isEmpty()) {
      logger.error(
          "Conversation {} does not contain any message. No summary to generate!",
          conversationEnded.conversationId());
      return;
    }

    var text =
        conversation.getMessages().stream()
            .filter(this::isAgentOrUser)
            .map(this::buildStringView)
            .collect(Collectors.joining("\n"));
    getAssistant()
        .chat(text)
        .onCompleteResponse(response -> saveMinute(response.aiMessage().text(), conversation))
        .onPartialResponse(logger::debug)
        .onError(it -> logger.error(it.getMessage(), it))
        .start();
  }

  private void saveMinute(String minute, Conversation conversation) {
    var summaryFile = createSummaryFilePath(conversation);
    try {
      Files.writeString(summaryFile, minute);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Path createSummaryFilePath(Conversation conversation) {
    var dataFolder = Path.of("./data");
    if (!dataFolder.toFile().isDirectory()) {
      try {
        Files.createDirectories(dataFolder);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    var fileName = "%s.minute.json".formatted(conversation.getId().value());
    return Path.of(dataFolder.toAbsolutePath().toString(), fileName).toAbsolutePath();
  }

  private String buildStringView(ConversationMessage message) {
    return "%s: %s".formatted(message.source(), message.text());
  }

  private boolean isAgentOrUser(ConversationMessage conversationMessage) {
    return conversationMessage.messageType().equals(ConversationMessageType.USER_MESSAGE)
        || conversationMessage.messageType().equals(ConversationMessageType.AGENT_MESSAGE);
  }

  @Override
  public List<Class<? extends SystemEvent>> getAcceptedSystemMessages() {
    return List.of(ConversationEnded.class);
  }
}
