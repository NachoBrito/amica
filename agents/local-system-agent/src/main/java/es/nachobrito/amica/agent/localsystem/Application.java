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

package es.nachobrito.amica.agent.localsystem;

import es.nachobrito.amica.domain.model.agent.conversation.ConversationMessage;
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import es.nachobrito.amica.domain.model.message.payload.ConversationEnded;
import es.nachobrito.amica.domain.model.message.payload.SequenceNumber;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.annotation.SerdeImport;

@SerdeImport(UserRequest.class)
@SerdeImport(AgentResponse.class)
@SerdeImport(SequenceNumber.class)
@SerdeImport(ConversationMessage.class)
@SerdeImport(ConversationMessage.ToolExecutionRequest.class)
@SerdeImport(ConversationEnded.class)
public class Application {

  public static void main(String[] args) {
    Micronaut.run(Application.class, args);
  }
}
