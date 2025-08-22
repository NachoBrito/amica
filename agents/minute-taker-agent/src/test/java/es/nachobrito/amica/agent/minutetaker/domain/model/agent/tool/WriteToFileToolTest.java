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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * @author nacho
 */
class WriteToFileToolTest {

  @Test
  void shouldWriteContentToFile() throws IOException {
    var tmpFile = Files.createTempFile("amica-agent-write-test", ".tmp");
    var content = "Content to add";

    new WriteToFileTool()
        .execute(new WriteToFileToolParams(tmpFile.toAbsolutePath().toString(), content));

    assertTrue(tmpFile.toFile().exists());
    var fileContent = Files.readString(tmpFile.toAbsolutePath());
    assertEquals(content, fileContent);
    tmpFile.toFile().deleteOnExit();
  }
}
