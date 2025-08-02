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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tool that can be used to get the current list of active processes, sorted by descending used cpu
 * percentage.
 *
 * @author nacho
 */
@Introspected
public class ProcessesTool implements Tool<ProcessesTool.Parameters, ProcessesTool.ProcessList> {

  @Introspected
  public record Parameters(int count) {}

  @Introspected
  public record ProcessList(List<Process> processes) {}

  @Introspected
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

  private static final String[] COMMAND =
      new String[] {
        "/usr/bin/ps", "-eo", "pid,tid,psr,pcpu,rss,args", "--sort=-pcpu,-pmem", "--no-headers"
      };

  @Override
  public String getDescription() {
    return "Returns a list of active processes";
  }

  @Override
  public Class<Parameters> getParameterClass() {
    return Parameters.class;
  }

  @Override
  public Class<ProcessList> getResultClass() {
    return ProcessList.class;
  }

  @Override
  public ProcessList execute(Parameters params) {
    var processes = new ArrayList<Process>();
    loadProcesses(params, processes);
    return new ProcessList(Collections.unmodifiableList(processes));
  }

  private static void loadProcesses(Parameters params, ArrayList<Process> processes) {
    try {
      var process = new ProcessBuilder().command(COMMAND).start();
      try (var reader = process.inputReader()) {
        reader
            .lines()
            .takeWhile(s -> processes.size() < params.count)
            .map(Process::of)
            .forEach(processes::add);

        process.destroyForcibly();
      }

    } catch (IOException e) {
      throw new ToolInvocationException(e);
    }
  }
}
