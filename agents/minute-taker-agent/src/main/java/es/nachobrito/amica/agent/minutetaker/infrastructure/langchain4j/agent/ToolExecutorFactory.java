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

import static dev.langchain4j.internal.Json.fromJson;
import static dev.langchain4j.internal.Json.toJson;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecutor;
import es.nachobrito.amica.domain.model.agent.tool.Tool;
import java.util.concurrent.ExecutionException;

/**
 * @author nacho
 */
public class ToolExecutorFactory {
  /**
   * Creates a ToolExecutor for running the provided tool. The tool will be executed using a
   * centralized service that will handle cross-cutting concerns, like authorization, monitoring,
   * caching, etc.
   *
   * @param <A> the tool's argument type
   * @param <R> the tool's return type
   * @param tool the tool to execute
   * @param toolExecutor the tool executor service
   * @return a new ToolExecutor to run the tool
   */
  static <A, R> ToolExecutor with(
      Tool<A, R> tool, es.nachobrito.amica.domain.model.agent.tool.ToolExecutor toolExecutor) {
    return new ToolExecutor() {
      @Override
      public String execute(ToolExecutionRequest toolExecutionRequest, Object memoryId) {
        var argumentClass = tool.getArgumentClass();
        var argument = fromJson(toolExecutionRequest.arguments(), argumentClass);
        try {
          var result = toolExecutor.executeAsync(tool, argumentClass.cast(argument)).get();
          return toJson(result);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
  }
}
