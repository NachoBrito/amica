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

package es.nachobrito.amica.domain.model.agent;

import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.MessageBus;
import es.nachobrito.amica.domain.model.message.MessagePayload;
import es.nachobrito.amica.domain.model.message.MessageTopic;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;

import java.util.List;

/**
 * @author nacho
 */
public interface Agent {

    AgentDetails getDetails();

    void onUserMessage(Message<UserRequest> userRequestMessage);

    void onSystemMessage(Message<?> systemMessage);

    List<Class<? extends MessagePayload>> getAcceptedSystemMessages();

    default void register(MessageBus bus) {
        bus.registerConsumer(MessageTopic.USER_REQUESTS, UserRequest.class, this::onUserMessage);
        getAcceptedSystemMessages()
                .forEach(type ->
                        bus.registerConsumer(MessageTopic.SYSTEM_EVENTS, type, this::onSystemMessage));
    }
}
