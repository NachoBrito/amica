package es.nachobrito.amica.domain.model.message;

/**
 * @author nacho
 */
public interface MessageBus {
    void send(Message<?> message);
}
