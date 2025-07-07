package es.nachobrito.amica.domain.model.message;

/**
 * @author nacho
 */
public interface MessageBus {
    void send(Message<?> message);
    <P extends MessagePayload> void registerConsumer(MessageTopic messageTopic, Class<P> payloadType, MessageConsumer<P> consumer);
}
