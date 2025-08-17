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

package es.nachobrito.amica.domain.model.agent.tool;

import java.util.concurrent.CompletableFuture;

/**
 * Implementations of this interface will handle centralized tool execution, to implement cross-cutting concerns like
 * observability, authorization, caching, etc.
 * <p>
 * Agents should use these services for tool execution.
 *
 * @author nacho
 */
public interface ToolExecutor {

    <A, R> R execute(Tool<A, R> tool, A arguments);

    <A, R> CompletableFuture<R> executeAsync(Tool<A, R> tool, A arguments);
}
