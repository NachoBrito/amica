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

package es.nachobrito.amica.agent.conversation.domain.model.message.consumer;

import es.nachobrito.amica.agent.conversation.domain.model.agent.Agent;
import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.MessageConsumer;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nacho
 */
@Singleton
public class ConversationConsumer implements MessageConsumer<UserRequest> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Agent agent;

  public ConversationConsumer(Agent agent) {
    this.agent = agent;
  }

  @Override
  public void consume(Message<UserRequest> message) {
    logger.info("Received user message: {}", message.toString());
    agent.onUserMessage(message);
  }
}
