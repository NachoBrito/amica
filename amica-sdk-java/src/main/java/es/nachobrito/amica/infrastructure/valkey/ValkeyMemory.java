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

import es.nachobrito.amica.domain.model.agent.Memory;
import es.nachobrito.amica.domain.model.agent.conversation.Conversation;
import es.nachobrito.amica.domain.model.agent.conversation.MessageSerializer;
import es.nachobrito.amica.domain.model.message.ConversationId;
import io.valkey.JedisPool;

/**
 * @author nacho
 */
public class ValkeyMemory implements Memory {
    private final MessageSerializer messageSerializer;
    private final String valkeyHost;
    private final int valkeyPort;

    private JedisPool jedisPool;

    public ValkeyMemory(MessageSerializer messageSerializer, String valkeyHost, int valkeyPort) {
        this.messageSerializer = messageSerializer;
        this.valkeyHost = valkeyHost;
        this.valkeyPort = valkeyPort;
    }

    private JedisPool getJedisPool() {
        if (jedisPool == null) {
            io.valkey.JedisPoolConfig config = new io.valkey.JedisPoolConfig();
            config.setMaxTotal(2);
            config.setMaxIdle(2);
            config.setMinIdle(1);
            this.jedisPool = new io.valkey.JedisPool(config, valkeyHost, valkeyPort);
        }
        return jedisPool;
    }

    @Override
    public Conversation getConversation(ConversationId conversationId) {
        return new ValkeyConversation(conversationId, getJedisPool(), messageSerializer);
    }
}
