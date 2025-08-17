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

package es.nachobrito.amica.infrastructure.lucene;

import es.nachobrito.amica.domain.model.agent.tool.Tool;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author nacho
 */
class LuceneToolManagerTest {

    private abstract class TestTool implements Tool<Void, Void> {

        @Override
        public Class<Void> getArgumentClass() {
            return Void.class;
        }

        @Override
        public Class<Void> getResultClass() {
            return Void.class;
        }

        @Override
        public Void execute(Void args) {
            return null;
        }
    }

    @Test
    void shouldReturnRelevantTools() {
        var notificationTool = new TestTool() {
            @Override
            public String getDescription() {
                return "Sends notifications to users about relevant topics.";
            }

            @Override
            public String getName() {
                return "NotificationTool";
            }
        };
        var weatherTool = new TestTool() {
            @Override
            public String getDescription() {
                return "Gets the current weather in the provided location";
            }

            @Override
            public String getName() {
                return "WeatherTool";
            }
        };
        var tools = Set.of(notificationTool, weatherTool);
        var toolManager = new LuceneToolManager(tools);

        assertEquals(notificationTool, toolManager.getRelevantTools("Notify the selected user").stream().findFirst().orElseThrow());
        assertEquals(weatherTool, toolManager.getRelevantTools("What is the weather like in London today?").stream().findFirst().orElseThrow());
    }

    @Test
    void shouldParsePrompts() {
        var prompt = """
                # Identity
                
                You are an assistant specialized on providing detailed information about the current system, such as cpu load, memory
                usage or running processes. You MUST use the provided tools to get the information requested.
                
                # Instructions
                
                - Current date is 2025-08-17T18:41:28.852692410
                - Use the functions — don’t guess.
                
                Respond to the following user message:
                
                What was the load average of this system in the last 5 minutes?
                
                """;
        var toolManager = new LuceneToolManager(Set.of());
        assertDoesNotThrow(() -> {
            toolManager.getRelevantTools(prompt);
        });
    }

}