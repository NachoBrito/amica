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

package es.nachobrito.amica.infrastructure.langchain4j;

import es.nachobrito.amica.domain.model.agent.tool.Tool;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author nacho
 */
class LangChain4JToolManagerTest {

    private abstract static class TestTool implements Tool<Void, Void> {

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
                return "Sends notifications to users about relevant topics";
            }

            @Override
            public String getName() {
                return "NotificationTool";
            }
        };
        var weatherTool = new TestTool() {
            @Override
            public String getDescription() {
                return "Provides weather information for a location";
            }

            @Override
            public String getName() {
                return "WeatherTool";
            }
        };
        var tools = Set.of(notificationTool, weatherTool);
        var toolManager = new LangChain4JToolManager();
        toolManager.addTools(tools);
        var relevantTools = toolManager.getRelevantTools("Notify the selected user");
        assertEquals(notificationTool, relevantTools.getFirst());

        relevantTools = toolManager.getRelevantTools("Will it rain in London tomorrow?");
        assertEquals(weatherTool, relevantTools.getFirst());
    }

}