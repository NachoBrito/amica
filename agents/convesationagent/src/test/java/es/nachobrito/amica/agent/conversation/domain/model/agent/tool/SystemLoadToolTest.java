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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author nacho
 */
class SystemLoadToolTest {
  @Test
  void shouldReturnSystemLoad() {
    var tool = new SystemLoadTool();
    var result = tool.execute(null);
    assertNotNull(result);
    assertTrue(result.loadAvg1() > 0);
    assertTrue(result.loadAvg5() > 0);
    assertTrue(result.loadAvg15() > 0);
    assertTrue(result.uptimeDays() > 0);
    assertTrue(result.userCount() > 0);
  }
}
