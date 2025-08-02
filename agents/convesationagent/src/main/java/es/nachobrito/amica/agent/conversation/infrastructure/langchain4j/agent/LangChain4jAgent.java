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

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.jlama.JlamaStreamingChatModel;
import es.nachobrito.amica.domain.model.agent.Agent;
import es.nachobrito.amica.domain.model.agent.Memory;
import es.nachobrito.amica.domain.model.agent.ToolManager;
import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.MessageBus;
import es.nachobrito.amica.domain.model.message.MessagePayload;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import io.micronaut.core.io.ResourceLoader;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author nacho
 */
@Singleton
public class LangChain4jAgent implements Agent {
  public static final String CHAT_MODEL = "tjake/Llama-3.2-1B-Instruct-JQ4";
  public static final float TEMPERATURE = 0.0f;

  private final MessageBus messageBus;
  private final ToolManager toolManager;
  private final Memory memory;
  private final ResourceLoader resourceLoader;

  private StreamingChatModel chatModel;
  private PromptTemplate systemPromptTemplate;

  public LangChain4jAgent(
      MessageBus messageBus,
      ToolManager toolManager,
      Memory memory,
      ResourceLoader resourceLoader) {
    this.messageBus = messageBus;
    this.toolManager = toolManager;
    this.memory = memory;
    this.resourceLoader = resourceLoader;
  }

  private StreamingChatModel getChatModel() {
    if (chatModel == null) {
      chatModel =
          JlamaStreamingChatModel.builder().modelName(CHAT_MODEL).temperature(TEMPERATURE).build();
    }
    return chatModel;
  }

  @Override
  public void onUserMessage(Message<UserRequest> userRequestMessage) {
    var payload = userRequestMessage.payload();
    var conversation = memory.getConversation(userRequestMessage.conversationId());

    var tools = toolManager.getRelevantTools(userRequestMessage.payload().message());
    var chatRequest =
        ChatRequest.builder()
            .messages(getSystemPrompt(), UserMessage.from(payload.userName(), payload.message()))
            .toolSpecifications(ToolSpecificationFactory.with(tools))
            .build();
    var handler = new AgentStreamingResponseHandler(messageBus, userRequestMessage);
    getChatModel().chat(chatRequest, handler);
  }

  @Override
  public void onSystemMessage(Message<?> message) {
    // TODO
  }

  @Override
  public List<Class<? extends MessagePayload>> getAcceptedPayloads() {
    return List.of();
  }

  /**
   * @see
   *     https://medium.com/@mypalwal/how-to-write-better-ai-prompts-gpt-4-1-llama-deepseek-with-markdown-xml-agentic-workflows-1de8686f809f
   * @return
   */
  private SystemMessage getSystemPrompt() {
    if (systemPromptTemplate == null) {
      try (var stream = resourceLoader.getResourceAsStream("system-prompt.md").orElseThrow()) {
        var template = new String(stream.readAllBytes());
        systemPromptTemplate = PromptTemplate.from(template);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return systemPromptTemplate.apply(Map.of()).toSystemMessage();
  }
}
