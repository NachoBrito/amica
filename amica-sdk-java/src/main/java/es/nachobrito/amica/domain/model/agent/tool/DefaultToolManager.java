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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Dummy implementation of the ToolManager interface, returning every tool for each user query.
 *
 * @author nacho
 */
public class DefaultToolManager implements ToolManager {

    private final Map<String, Tool<?, ?>> tools;

    public DefaultToolManager(Set<Tool<?, ?>> tools) {
        this.tools = tools.stream().collect(Collectors.toMap(Tool::getName, Function.identity()));
    }

    @Override
    public Set<Tool<?, ?>> getRelevantTools(String userQuery) {
        return Set.copyOf(tools.values());
    }

    @Override
    public Optional<Tool<?, ?>> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }
}
