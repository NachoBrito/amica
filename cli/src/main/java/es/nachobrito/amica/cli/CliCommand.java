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

package es.nachobrito.amica.cli;

import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.MessageBus;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import es.nachobrito.amica.domain.model.message.payload.SequenceNumber;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@SerdeImport(UserRequest.class)
@SerdeImport(AgentResponse.class)
@SerdeImport(SequenceNumber.class)
@Command(name = "cli", description = "...",
        mixinStandardHelpOptions = true)
public class CliCommand implements Runnable {
    @Inject
    MessageBus messageBus;

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(CliCommand.class, args);
    }

    public void run() {
        var console = System.console();
        String userInput = console.readLine();
        try (var writer = console.writer()) {
            while (!"exit".equals(userInput)) {
                if (!userInput.isBlank()) {
                    var userMessage = Message.userRequest(new UserRequest(
                            ZonedDateTime.now(ZoneOffset.UTC),
                            "nacho",
                            userInput
                    ));

                    messageBus.send(userMessage, response -> {
                        writer.print(response.payload().message());
                        if (response.payload().isComplete()) {
                            writer.println("---");
                        }
                        writer.flush();
                    });
                }

                userInput = console.readLine();
            }
        }
        System.exit(0);
    }
}
