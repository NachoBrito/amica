package es.nachobrito.amica.agent.conversation.infrastructure.bus;

import es.nachobrito.amica.agent.conversation.domain.model.message.consumer.ConversationConsumer;
import es.nachobrito.amica.domain.model.message.MessageBus;
import es.nachobrito.amica.domain.model.message.MessagePayload;
import es.nachobrito.amica.domain.model.message.MessageTopic;
import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nacho
 */
@Singleton
public class BusInitializer implements ApplicationEventListener<StartupEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageBus messageBus;
    private final ConversationConsumer conversationConsumer;

    public BusInitializer(MessageBus messageBus, ConversationConsumer conversationConsumer) {
        this.messageBus = messageBus;
        this.conversationConsumer = conversationConsumer;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        logger.info("Registering listener for {}", MessageTopic.USER_REQUESTS.name());
        messageBus.registerConsumer(MessageTopic.USER_REQUESTS, UserRequest.class, conversationConsumer);
    }
}
