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

package es.nachobrito.amica.domain.model.agent.conversation;

import es.nachobrito.amica.domain.model.message.ConversationId;

import java.util.List;

/**
 * A Conversation instance holds the messages of a conversation between the user and one or more agents.
 *
 * @author nacho
 */
public interface Conversation {


    ConversationId getId();

    List<ConversationMessage> getMessages();

    void add(ConversationMessage conversationMessage);
}
