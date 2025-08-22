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

package es.nachobrito.amica.agent.minutetaker.domain.model.agent.tool;

import es.nachobrito.amica.domain.model.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author nacho
 */
public class WriteToFileTool implements Tool<WriteToFileToolParams, Void> {
  @Override
  public String getDescription() {
    return "Write text to a file in the local system.";
  }

  @Override
  public Class<WriteToFileToolParams> getArgumentClass() {
    return WriteToFileToolParams.class;
  }

  @Override
  public Class<Void> getResultClass() {
    return Void.class;
  }

  @Override
  public Void execute(WriteToFileToolParams writeToFileToolParams) {
    try {
      var path = Paths.get(writeToFileToolParams.filePath());
      var mode = path.toFile().exists() ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;
      Files.write(path, writeToFileToolParams.text().getBytes(), mode);
    } catch (IOException e) {
      throw new ToolInvocationException(e);
    }
    return null;
  }
}
