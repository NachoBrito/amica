package es.nachobrito.amica.agent.conversation.domain.model.message.consumer;

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

    @Override
    public void consume(Message<UserRequest> message) {
        logger.info(message.toString());
    }
}
