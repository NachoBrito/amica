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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author nacho
 */
public record ProcessList(List<Process> processes) {
  private static final String[] COMMAND =
      new String[] {
        "/usr/bin/ps", "-eo", "pid,tid,psr,pcpu,rss,args", "--sort=-pcpu,-pmem", "--no-headers"
      };

  public record Process(
      int pid, int processorNumber, String name, double cpuPercentage, double ramMb) {
    public static Process of(String line) {
      //    PID     TID PSR %CPU %MEM COMMAND
      //  96681   96681   2  200  0.0 ps
      var parts = line.trim().split("\\s+");
      var command = Arrays.stream(parts, 5, parts.length).collect(Collectors.joining(" "));
      return new Process(
          Integer.parseInt(parts[0]),
          Integer.parseInt(parts[2]),
          command,
          Double.parseDouble(parts[3]),
          Double.parseDouble(parts[4]) / 1024);
    }
  }

  public static ProcessList current(int processCount) {
    try {
      var processes = new ArrayList<Process>();
      var process = new ProcessBuilder().command(COMMAND).start();
      try (var reader = process.inputReader()) {
        reader
            .lines()
            .takeWhile(s -> processes.size() < processCount)
            .map(Process::of)
            .forEach(processes::add);

        process.destroyForcibly();
      }
      return new ProcessList(Collections.unmodifiableList(processes));
    } catch (IOException e) {
      throw new ToolInvocationException(e);
    }
  }
}
