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
import es.nachobrito.amica.domain.model.message.payload.AgentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * The MQTT protocol does not guarantee message ordering. Since we are creating a dedicated topic per conversation,
 * we expect very little number of unordered messages, so we will use this simple buffer class as a wrapper on the
 * original message consumer.
 * <p>
 * Objects of this class will store a buffer of messages in a tree map to sort them as they come.
 *
 * @author nacho
 */
public class ConversationBuffer implements Consumer<Message<AgentResponse>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageConsumer<AgentResponse> delegate;

    private final TreeMap<Integer, Message<AgentResponse>> buffer = new TreeMap<>();
    private int nextExpectedSeq = 1;
    private final int maxBufferSize = 50;
    private final Duration maxWait = Duration.ofMillis(1500);
    private Instant lastEmit = Instant.now();
    private final Runnable responseCompleteCallback;

    public ConversationBuffer(MessageConsumer<AgentResponse> delegate, Runnable responseCompleteCallback) {
        this.delegate = delegate;
        this.responseCompleteCallback = responseCompleteCallback;
    }


    @Override
    public synchronized void accept(Message<AgentResponse> message) {
        logger.debug("Received response {}", message);
        boolean responseComplete = false;
        int seq = message.payload().sequenceNumber().value();

        if (!buffer.containsKey(seq)) {
            buffer.put(seq, message);
        }

        while (buffer.containsKey(nextExpectedSeq)) {
            var next = buffer.remove(nextExpectedSeq);
            delegate.consume(next);
            lastEmit = Instant.now();
            nextExpectedSeq++;
            if (Boolean.TRUE.equals(next.payload().isComplete())) {
                responseComplete = true;
            }
        }

        if (!buffer.isEmpty()) {
            if (buffer.size() > maxBufferSize || Duration.between(lastEmit, Instant.now()).compareTo(maxWait) > 0) {
                var first = buffer.pollFirstEntry();
                if (first != null) {
                    delegate.consume(first.getValue());
                    lastEmit = Instant.now();
                    nextExpectedSeq = first.getKey() + 1;
                    if (Boolean.TRUE.equals(first.getValue().payload().isComplete())) {
                        responseComplete = true;
                    }
                }
            }
        }

        if (responseComplete) {
            responseCompleteCallback.run();
        }
    }
}
