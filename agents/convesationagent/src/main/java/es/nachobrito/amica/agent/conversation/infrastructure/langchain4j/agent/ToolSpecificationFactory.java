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

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.output.JsonSchemas;
import es.nachobrito.amica.domain.model.agent.tool.Tool;
import java.util.List;
import java.util.Set;

/**
 * @author nacho
 */
public class ToolSpecificationFactory {

  /**
   * Creates a new ToolSpecification describing the provided tool instance
   *
   * @param tool the tool to describe with this ToolSpecification
   * @return the ToolSpecification
   */
  public static ToolSpecification with(Tool<?, ?> tool) {
    var builder =
        ToolSpecification.builder().name(tool.getName()).description(tool.getDescription());

    buildParameters(tool, builder);
    return builder.build();
  }

  private static void buildParameters(Tool<?, ?> tool, ToolSpecification.Builder toolSpecBuilder) {
    var voidClasses = Set.of(void.class, Void.class);
    if (!voidClasses.contains(tool.getArgumentClass())) {
      var builder = JsonObjectSchema.builder();
      var propertySchema = JsonSchemas.jsonSchemaFrom(tool.getArgumentClass());
      builder.addProperty("params", propertySchema.orElseThrow().rootElement());
      toolSpecBuilder.parameters(builder.build());
    }
  }

  public static List<ToolSpecification> with(Set<Tool<?, ?>> tools) {
    return tools.stream().map(ToolSpecificationFactory::with).toList();
  }
}
