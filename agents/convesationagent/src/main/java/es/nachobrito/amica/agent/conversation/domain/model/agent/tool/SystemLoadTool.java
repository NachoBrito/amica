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

import es.nachobrito.amica.domain.model.agent.Tool;
import io.micronaut.core.annotation.Introspected;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Tool that can be used to get the current uptime and average load from the system
 *
 * @author nacho
 */
@Introspected
public class SystemLoadTool implements Tool<Void, SystemLoadTool.SystemLoad> {
  private static final Pattern pattern =
      Pattern.compile(
          "^([\\d:]+) up (\\d+) days, ([\\d:]+),  (\\d+) users,  load average: ([\\d\\.]+), ([\\d\\.]+), ([\\d\\.]+)$");

  @Introspected
  public record SystemLoad(
      int uptimeDays, int userCount, double loadAvg1, double loadAvg5, double loadAvg15) {
    public static SystemLoad of(String line) {
      var matcher = pattern.matcher(line.trim());
      if (!matcher.matches()) {
        throw new ToolInvocationException("Invalid format: %s".formatted(line));
      }
      return new SystemLoad(
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(4)),
          Double.parseDouble(matcher.group(5)),
          Double.parseDouble(matcher.group(6)),
          Double.parseDouble(matcher.group(7)));
    }
  }

  private static final String[] COMMAND = new String[] {"uptime"};

  @Override
  public String getDescription() {
    return "Returns information about the current system load, uptime and current user count";
  }

  @Override
  public Class<Void> getParameterClass() {
    return Void.class;
  }

  @Override
  public Class<SystemLoad> getResultClass() {
    return SystemLoad.class;
  }

  @Override
  public SystemLoad execute(Void params) {
    try {
      var process = new ProcessBuilder().command(COMMAND).start();
      try (var reader = process.inputReader()) {
        var load = reader.lines().findFirst().map(SystemLoad::of).orElseThrow();

        process.destroyForcibly();
        return load;
      }

    } catch (IOException e) {
      throw new ToolInvocationException(e);
    }
  }
}
