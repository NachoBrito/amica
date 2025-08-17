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

import es.nachobrito.amica.domain.model.agent.conversation.Conversation;
import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import es.nachobrito.amica.domain.model.agent.conversation.MessageSerializer;
import es.nachobrito.amica.domain.model.message.ConversationId;
import io.valkey.JedisPool;

import java.time.Duration;
import java.util.List;

/**
 * @author nacho
 */
public class ValkeyConversation implements Conversation {

    private final ConversationId conversationId;
    private final JedisPool jedisPool;
    private final MessageSerializer messageSerializer;
    private static final long CONVERSATION_TIMEOUT = Duration.ofHours(1).toMillis();

    public ValkeyConversation(ConversationId conversationId, JedisPool jedis, MessageSerializer messageSerializer) {
        this.conversationId = conversationId;
        this.jedisPool = jedis;
        this.messageSerializer = messageSerializer;
    }


    @Override
    public ConversationId getId() {
        return conversationId;
    }

    @Override
    public List<ConversationMessage> getMessages() {
        try (var jedis = jedisPool.getResource()) {
            var conversationKey = getConversationKey();
            var items = jedis.lrange(conversationKey, 0, -1);
            return items.stream().map(messageSerializer::deSerializeMessage).toList();
        }
    }

    @Override
    public void add(ConversationMessage conversationMessage) {
        try (var jedis = jedisPool.getResource()) {
            var conversationKey = getConversationKey();
            var serialized = messageSerializer.serializeMessage(conversationMessage);
            jedis.rpush(conversationKey, serialized);
            jedis.expire(conversationKey, CONVERSATION_TIMEOUT);
        }
    }

    private String getConversationKey() {
        return "conversation:%s".formatted(conversationId.value());
    }
}
