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

package es.nachobrito.amica.infrastructure.valkey;

import es.nachobrito.amica.domain.model.agent.AgentDetails;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import es.nachobrito.amica.domain.model.agent.conversation.MessageSerializer;
import es.nachobrito.amica.domain.model.message.ConversationId;
import es.nachobrito.amica.infrastructure.JacksonMessageSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * @author nacho
 */
public class ValkeyConversationIT {
    private final MessageSerializer messageSerializer = new JacksonMessageSerializer();

    @Test
    void expectConversationSaved() {
        var memory = new ValkeyMemory(messageSerializer, "localhost", 6379);
        var conversationId = ConversationId.newRandom();
        var conversation = memory.getConversation(conversationId);
        var messages = List.of(
                ConversationMessage.ofUserMessage("User Name", "user message"),
                ConversationMessage.ofToolExecution(new ConversationMessage.ToolExecutionRequest("id", "tool name", "arguments"), "result"),
                ConversationMessage.ofAgentResponse(new AgentDetails("agent-id", "Test Agent"), "agent response", "agent thinking", Set.of())
        );
        messages.forEach(conversation::add);

        assertArrayEquals(
                messages.toArray(new ConversationMessage[]{}),
                conversation.getMessages().toArray(new ConversationMessage[]{}));

    }
}
