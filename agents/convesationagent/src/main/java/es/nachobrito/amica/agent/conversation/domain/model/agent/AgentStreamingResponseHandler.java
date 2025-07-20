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

package es.nachobrito.amica.agent.conversation.domain.model.agent;


import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.MessageBus;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import es.nachobrito.amica.domain.model.message.payload.SequenceNumber;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nacho
 */
public class AgentStreamingResponseHandler implements StreamingChatResponseHandler {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final MessageBus messageBus;
  private final Message<UserRequest> userMessage;

  private final AtomicInteger sequence = new AtomicInteger(0);

  public AgentStreamingResponseHandler(MessageBus messageBus, Message<UserRequest> userMessage) {
    this.messageBus = messageBus;
    this.userMessage = userMessage;
  }

  @Override
  public void onPartialResponse(String partialResponse) {
    var agentResponse =
        new AgentResponse(
            partialResponse,
            ZonedDateTime.now(ZoneOffset.UTC),
            false,
            new SequenceNumber(sequence.incrementAndGet()));
    var responseMessage = Message.responseTo(userMessage, agentResponse);
    messageBus.respond(userMessage.id(), responseMessage);
  }

  @Override
  public void onCompleteResponse(ChatResponse completeResponse) {
    // todo: handle tool execution requests
    var agentResponse =
        new AgentResponse(
            "", ZonedDateTime.now(ZoneOffset.UTC), true, new SequenceNumber(sequence.incrementAndGet()));
    var responseMessage = Message.responseTo(userMessage, agentResponse);
    messageBus.respond(userMessage.id(), responseMessage);
  }

  @Override
  public void onError(Throwable error) {
    logger.error(error.getMessage(), error);
  }
}
