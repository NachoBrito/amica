package es.nachobrito.amica.domain.model.message;

/**
 * @author nacho
 */
public interface PayloadSerializer {
    String serialize(MessagePayload payload);
    <P extends MessagePayload> P deSerialize(String serialized, Class<P> type);
}
