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

import dev.langchain4j.model.jlama.JlamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProviderResult;
import es.nachobrito.amica.domain.model.agent.AgentDetails;
import es.nachobrito.amica.domain.model.agent.Memory;
import es.nachobrito.amica.domain.model.agent.tool.ToolExecutor;
import es.nachobrito.amica.domain.model.agent.tool.ToolManager;
import es.nachobrito.amica.domain.model.message.ConversationId;

/**
 * @author nacho
 */
public class AIAssistantFactory {
  public static final String CHAT_MODEL = "tjake/Qwen2.5-0.5B-Instruct-JQ4";
  // public static final String CHAT_MODEL = "tjake/Llama-3.2-1B-Instruct-JQ4";
  // public static final String CHAT_MODEL = "tjake/Mistral-7B-Instruct-v0.3-JQ4";
  public static final float TEMPERATURE = 0.0f;

  /**
   * Creates an AIAssistant instance with the provided resources
   *
   * @param memory the Memory implementation for localsystem management
   * @param toolManager the ToolManager implementation for tool access
   * @param toolExecutor
   * @param agentDetails the AgentDetails instance with information about the current Agent
   * @return the new AIAssistant instance
   */
  static AIAssistant with(
      Memory memory,
      ToolManager toolManager,
      ToolExecutor toolExecutor,
      AgentDetails agentDetails) {
    var chatModel =
        JlamaStreamingChatModel.builder().modelName(CHAT_MODEL).temperature(TEMPERATURE).build();

    return AiServices.builder(AIAssistant.class)
        .streamingChatModel(chatModel)
        .chatMemoryProvider(
            memoryId ->
                ChatMemoryFactory.with(
                    memory.getConversation(new ConversationId(memoryId.toString())), agentDetails))
        .toolProvider(
            toolProviderRequest -> {
              var builder = ToolProviderResult.builder();
              var userRequest = toolProviderRequest.userMessage().singleText();
              toolManager
                  .getRelevantTools(userRequest)
                  .forEach(
                      tool -> {
                        var toolSpecification = ToolSpecificationFactory.with(tool);
                        var executor = ToolExecutorFactory.with(tool, toolExecutor);
                        builder.add(toolSpecification, executor);
                      });
              return builder.build();
            })
        .build();
  }
}
