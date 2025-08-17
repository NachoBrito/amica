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

package es.nachobrito.amica.infrastructure.hivemqtt;

import es.nachobrito.amica.domain.model.message.Message;
import es.nachobrito.amica.domain.model.message.MessageConsumer;
import es.nachobrito.amica.domain.model.message.MessagePayload;

/**
 * @author nacho
 */
public record TopicHandler<P extends MessagePayload>(Class<P> payloadType,
                                                     MessageConsumer<P> consumer) {
    @SuppressWarnings("unchecked")
    public void consume(Message<?> message) {
        var payload = message.payload();
        if (payloadType.isAssignableFrom(payload.getClass())) {
            consumer.consume((Message<P>) message);
        }
    }
}
