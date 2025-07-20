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

package es.nachobrito.amica.agent.conversation.domain.model.agent;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.jlama.JlamaStreamingChatModel;
import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.MessageBus;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import jakarta.inject.Singleton;

/**
 * @author nacho
 */
@Singleton
public class DefaultAgent implements Agent {
  public static final String CHAT_MODEL = "tjake/Llama-3.2-1B-Instruct-JQ4";
  public static final float TEMPERATURE = 0.3f;
  public static final String SYSTEM_PROMPT =
      "You are a helpful chatbot that answers questions in under 30 words.";
  private final MessageBus messageBus;
  private final ToolManager toolManager;
  private final Memory memory;

  private StreamingChatModel chatModel;

  public DefaultAgent(MessageBus messageBus, ToolManager toolManager, Memory memory) {
    this.messageBus = messageBus;
    this.toolManager = toolManager;
    this.memory = memory;
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
            .messages(
                SystemMessage.from(SYSTEM_PROMPT),
                UserMessage.from(payload.userName(), payload.message()))
            .build();
    var handler = new AgentStreamingResponseHandler(messageBus, userRequestMessage);
    getChatModel().chat(chatRequest, handler);
  }
}
