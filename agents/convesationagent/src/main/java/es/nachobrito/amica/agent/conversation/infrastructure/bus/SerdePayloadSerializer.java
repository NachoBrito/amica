package es.nachobrito.amica.agent.conversation.infrastructure.bus;

import es.nachobrito.amica.domain.model.message.MessagePayload;
import es.nachobrito.amica.domain.model.message.PayloadSerializer;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * @author nacho
 */
@Singleton
public class SerdePayloadSerializer implements PayloadSerializer {
    private final ObjectMapper objectMapper;

    public SerdePayloadSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(MessagePayload messagePayload) {
        try {

            return objectMapper.writeValueAsString(messagePayload);
        } catch (IOException e) {
            var error = "Cannot serialize payload: %s".formatted(messagePayload);
            throw new MessageSerializationException(error, e);
        }
    }

    @Override
    public <P extends MessagePayload> P deSerialize(String s, Class<P> aClass) {
        try {
            return objectMapper.readValue(s, aClass);
        } catch (IOException e) {
            var error = "Cannot deserialize string: %s".formatted(s);
            throw new MessageSerializationException(error, e);
        }
    }
}
