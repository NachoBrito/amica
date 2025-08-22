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

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author nacho
 */
public record SystemLoad(
    int uptimeDays, int userCount, double loadAvg1, double loadAvg5, double loadAvg15) {

  private static final Pattern pattern =
      Pattern.compile(
          "^([\\d:]+) up (\\d+) days,\\s+([\\d:\\w\\s]+),\\s+(\\d+) users,\\s+load average: ([\\d\\.]+), ([\\d\\.]+), ([\\d\\.]+)$");

  static final String[] COMMAND = new String[] {"uptime"};

  private static SystemLoad of(String line) {
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

  public static SystemLoad current() {
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
