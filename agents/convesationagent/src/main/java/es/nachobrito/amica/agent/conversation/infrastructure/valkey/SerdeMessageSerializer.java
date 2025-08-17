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

package es.nachobrito.amica.agent.conversation.infrastructure.valkey;

import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import es.nachobrito.amica.domain.model.agent.conversation.MessageSerializer;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import java.io.IOException;

/**
 * @author nacho
 */
@Singleton
public class SerdeMessageSerializer implements MessageSerializer {
  private final ObjectMapper objectMapper;

  public SerdeMessageSerializer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String serializeMessage(ConversationMessage conversationMessage) {
    try {
      return objectMapper.writeValueAsString(conversationMessage);
    } catch (IOException e) {
      var error = "Cannot serialize message: %s".formatted(conversationMessage);
      throw new MessageSerializationException(error, e);
    }
  }

  @Override
  public ConversationMessage deSerializeMessage(String s) {
    try {
      return objectMapper.readValue(s, ConversationMessage.class);
    } catch (IOException e) {
      var error = "Cannot deserialize message: %s".formatted(s);
      throw new MessageSerializationException(error, e);
    }
  }
}
