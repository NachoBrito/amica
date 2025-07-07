package es.nachobrito.amica.domain.model.message;

/**
 * @author nacho
 */
@FunctionalInterface
public interface MessageConsumer<P extends MessagePayload> {
    void consume(Message<P> message);
}
