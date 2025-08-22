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

package es.nachobrito.amica.agent.localsystem.domain.model.agent.tool;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author nacho
 */
class SystemLoadToolTest {
  @Test
  void shouldReturnUptime() {
    var tool = new SystemLoadTool();
    var load = tool.execute(null);
    assertNotNull(load);

    assertTrue(load.loadAvg1() > 0);
    assertTrue(load.loadAvg5() > 0);
    assertTrue(load.loadAvg15() > 0);
    assertTrue(load.uptimeDays() >= 0);
    assertTrue(load.userCount() > 1);
  }
}
