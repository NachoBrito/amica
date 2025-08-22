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

import static es.nachobrito.amica.agent.localsystem.infrastructure.langchain4j.agent.AIAssistantFactory.CHAT_MODEL;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import es.nachobrito.amica.domain.model.agent.Agent;
import es.nachobrito.amica.domain.model.agent.AgentDetails;
import es.nachobrito.amica.domain.model.agent.Memory;
import es.nachobrito.amica.domain.model.agent.tool.ToolExecutor;
import es.nachobrito.amica.domain.model.agent.tool.ToolManager;
import es.nachobrito.amica.domain.model.message.AgentExecutionException;
import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.MessageBus;
import es.nachobrito.amica.domain.model.message.MessagePayload;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import es.nachobrito.amica.domain.model.message.payload.SequenceNumber;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import jakarta.inject.Singleton;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nacho
 */
@Singleton
public class LocalSystemAgent implements Agent {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final AgentDetails agentDetails =
      new AgentDetails(
          "localsystem-agent", "Conversation Agent (Langchain4J/JLama/%s)".formatted(CHAT_MODEL));

  private final MessageBus messageBus;
  private final ToolManager toolManager;
  private final ToolExecutor toolExecutor;
  private final Memory memory;

  private AIAssistant assistant;
  private PromptTemplate systemPromptTemplate;

  public LocalSystemAgent(
      MessageBus messageBus, ToolManager toolManager, ToolExecutor toolExecutor, Memory memory) {
    this.messageBus = messageBus;
    this.toolManager = toolManager;
    this.memory = memory;
    this.toolExecutor = toolExecutor;
  }

  private AIAssistant getAssistant() {
    if (assistant == null) {
      assistant = AIAssistantFactory.with(memory, toolManager, toolExecutor, agentDetails);
    }
    return assistant;
  }

  @Override
  public AgentDetails getDetails() {
    return agentDetails;
  }

  @Override
  public void onUserMessage(Message<UserRequest> userMessage) {
    var payload = userMessage.payload();

    TokenStream tokenStream =
        getAssistant()
            .chat(userMessage.conversationId().value(), payload.userName(), payload.message());

    var sequence = new AtomicInteger();

    tokenStream
        .onPartialResponse(this::onPartialResponse)
        .onPartialThinking(this::onPartialThinking)
        .onRetrieved(this::onContentRetrieved)
        .onIntermediateResponse(this::onIntermediateResponse)
        .onToolExecuted(this::onToolExecuted)
        .onCompleteResponse(
            (ChatResponse response) ->
                publishCompleteResponse(userMessage, response, sequence.getAndIncrement()))
        .onError((Throwable error) -> onError(userMessage, error))
        .start();
  }

  private void onError(Message<UserRequest> userMessage, Throwable error) {
    logger.error(error.getMessage(), error);
    throw new AgentExecutionException(userMessage, error);
  }

  private void publishCompleteResponse(
      Message<UserRequest> userMessage, ChatResponse response, int sequenceNumber) {
    messageBus.respond(
        userMessage.id(),
        Message.responseTo(
            userMessage,
            new AgentResponse(
                response.aiMessage().text(),
                ZonedDateTime.now(),
                true,
                new SequenceNumber(sequenceNumber))));
  }

  private void onToolExecuted(ToolExecution toolExecution) {
    logger.debug("Tool executed: {}", toolExecution);
  }

  private void onIntermediateResponse(ChatResponse chatResponse) {
    logger.debug("Intermediate response: {}", chatResponse.aiMessage().text());
  }

  private void onContentRetrieved(List<Content> contents) {
    logger.debug("Content retrieved: {}", contents);
  }

  private void onPartialThinking(PartialThinking partialThinking) {
    logger.debug("Partial thinking: {}", partialThinking.text());
  }

  private void onPartialResponse(String partialResponse) {
    logger.debug("Partial: {}", partialResponse);
  }

  @Override
  public void onSystemMessage(Message<?> message) {
    // TODO
  }

  @Override
  public List<Class<? extends MessagePayload>> getAcceptedSystemMessages() {
    return List.of();
  }
}
