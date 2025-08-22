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

import es.nachobrito.amica.domain.model.agent.tool.Tool;

/**
 * @author nacho
 */
public class LoadAverage15Tool implements Tool<Void, Double> {
  @Override
  public String getDescription() {
    return "Returns the average system load in the past 15 minutes";
  }

  @Override
  public Class<Void> getArgumentClass() {
    return void.class;
  }

  @Override
  public Class<Double> getResultClass() {
    return Double.class;
  }

  @Override
  public Double execute(Void unused) {
    return SystemLoad.current().loadAvg15();
  }
}
