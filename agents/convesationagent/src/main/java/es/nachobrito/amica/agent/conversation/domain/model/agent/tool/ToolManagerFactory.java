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

package es.nachobrito.amica.agent.conversation.domain.model.agent.tool;

import es.nachobrito.amica.domain.model.agent.tool.ToolManager;
import es.nachobrito.amica.infrastructure.lucene.LuceneToolManager;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import java.util.Set;

/**
 * @author nacho
 */
@Factory
public class ToolManagerFactory {
  @Singleton
  ToolManager toolManager() {
    return new LuceneToolManager(
        Set.of(new LoadAverage5Tool(), new UserCountTool(), new UptimeTool()));
  }
}
