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
import es.nachobrito.amica.domain.model.agent.Tool;
import java.util.List;
import java.util.Set;

/**
 * @author nacho
 */
public class ToolSpecificationFactory {

  public static ToolSpecification with(Tool<?, ?> tool) {
    return ToolSpecification.builder()
        .name(tool.getClass().getName())
        .description(tool.getDescription())
        .parameters(buildParameters(tool))
        .build();
  }

  private static JsonObjectSchema buildParameters(Tool<?, ?> tool) {
    var builder = JsonObjectSchema.builder();
    if (!Void.class.equals(tool.getParameterClass())) {
      var propertySchema = JsonSchemas.jsonSchemaFrom(tool.getParameterClass());
      builder.addProperty("params", propertySchema.orElseThrow().rootElement());
    }
    return builder.build();
  }

  public static List<ToolSpecification> with(Set<Tool<?, ?>> tools) {
    return tools.stream().map(ToolSpecificationFactory::with).toList();
  }
}
