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
import io.valkey.Jedis;
import io.valkey.JedisPool;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author nacho
 */
class ValkeyConversationTest {

    private final MessageSerializer messageSerializer = new JacksonMessageSerializer();

    @Test
    void shouldReturnCorrectConversationId() {
        var jedisPool = mock(JedisPool.class);
        var conversationId = ConversationId.newRandom();
        var conversation = new ValkeyConversation(conversationId, jedisPool, messageSerializer);
        assertEquals(conversationId, conversation.getId());
    }

    @Test
    void shouldReturnConversationMessages() {
        var jedisPool = mock(JedisPool.class);
        var jedis = mock(Jedis.class);
        doReturn(jedis).when(jedisPool).getResource();

        var conversationId = ConversationId.newRandom();
        var conversationKey = "conversation:%s".formatted(conversationId.value());
        var messages = List.of(
                ConversationMessage.ofUserMessage("User Name", "user message"),
                ConversationMessage.ofToolExecution(new ConversationMessage.ToolExecutionRequest("id", "tool name", "arguments"), "tool result"),
                ConversationMessage.ofAgentResponse(new AgentDetails("agent-id", "Test Agent", true, true), "agent response", "thinking", Set.of())
        );
        var serialized = messages.stream().map(messageSerializer::serializeMessage).toList();
        doReturn(serialized).when(jedis).lrange(eq(conversationKey), eq(0L), eq(-1L));
        var conversation = new ValkeyConversation(conversationId, jedisPool, messageSerializer);

        assertArrayEquals(
                messages.toArray(new ConversationMessage[]{}),
                conversation.getMessages().toArray(new ConversationMessage[]{}));
    }

    @Test
    void shouldAddMessagesInTheRightOrder() {
        var jedisPool = mock(JedisPool.class);
        var jedis = mock(Jedis.class);
        doReturn(jedis).when(jedisPool).getResource();
        var conversationId = ConversationId.newRandom();
        var conversationKey = "conversation:%s".formatted(conversationId.value());
        var message = ConversationMessage.ofUserMessage("User Name", "user message");
        var serialized = messageSerializer.serializeMessage(message);
        var ttl = Duration.ofHours(1).toMillis();
        var conversation = new ValkeyConversation(conversationId, jedisPool, messageSerializer);
        conversation.add(message);

        verify(jedis).rpush(conversationKey, serialized);
        verify(jedis).expire(conversationKey, ttl);

    }
}