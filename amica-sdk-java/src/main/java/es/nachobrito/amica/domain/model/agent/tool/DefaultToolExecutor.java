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
 * TODO: implement result caching, etc
 *
 * @author nacho
 */
public class DefaultToolExecutor implements ToolExecutor {
    @Override
    public <A, R> R execute(Tool<A, R> tool, A arguments) {
        return tool.execute(arguments);
    }

    @Override
    public <A, R> CompletableFuture<R> executeAsync(Tool<A, R> tool, A arguments) {
        return CompletableFuture.supplyAsync(() -> tool.execute(arguments));
    }
}
