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

package es.nachobrito.amica.agent.minutetaker.infrastructure.valkey;

import es.nachobrito.amica.domain.model.agent.Memory;
import es.nachobrito.amica.infrastructure.valkey.ValkeyMemory;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

/**
 * @author nacho
 */
@Factory
public class ValkeyMemoryFactory {
  private final SerdeMessageSerializer messageSerializer;

  public ValkeyMemoryFactory(SerdeMessageSerializer messageSerializer) {
    this.messageSerializer = messageSerializer;
  }

  @Singleton
  Memory memory(
      @Value("${amika.valkey.host}") String valkeyHost,
      @Value("${amika.valkey.port}") int valkeyPort) {
    return new ValkeyMemory(messageSerializer, valkeyHost, valkeyPort);
  }
}
