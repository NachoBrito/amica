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

package es.nachobrito.amica.agent.minutetaker.infrastructure.mqtt;

import es.nachobrito.amica.domain.model.message.MessagePayload;
import es.nachobrito.amica.domain.model.message.PayloadSerializer;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import java.io.IOException;

/**
 * @author nacho
 */
@Singleton
public class SerdePayloadSerializer implements PayloadSerializer {
  private final ObjectMapper objectMapper;

  public SerdePayloadSerializer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String serialize(MessagePayload messagePayload) {
    try {

      return objectMapper.writeValueAsString(messagePayload);
    } catch (IOException e) {
      var error = "Cannot serialize payload: %s".formatted(messagePayload);
      throw new PayloadSerializationException(error, e);
    }
  }

  @Override
  public <P extends MessagePayload> P deSerialize(String s, Class<P> aClass) {
    try {
      return objectMapper.readValue(s, aClass);
    } catch (IOException e) {
      var error = "Cannot deserialize string: %s".formatted(s);
      throw new PayloadSerializationException(error, e);
    }
  }
}
