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

package es.nachobrito.amica.domain.model.message;

import es.nachobrito.amica.domain.model.message.payload.UserRequest;

/**
 * @author nacho
 */
public class AgentExecutionException extends RuntimeException {
    private final Message<UserRequest> originalMessage;
    private final Throwable cause;

    public AgentExecutionException(Message<UserRequest> originalMessage, Throwable cause) {
        super("Error while processing user message: %s".formatted(originalMessage.payload().message()), cause);
        this.originalMessage = originalMessage;
        this.cause = cause;
    }

    public Message<UserRequest> getOriginalMessage() {
        return originalMessage;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
