package es.nachobrito.amica.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.nachobrito.amica.domain.model.message.MessagePayload;
import es.nachobrito.amica.domain.model.message.PayloadSerializer;

/**
 * @author nacho
 */
public class JacksonPayloadSerializer implements PayloadSerializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serialize(MessagePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <P extends MessagePayload> P deSerialize(String serialized, Class<P> type) {
        try {
            return objectMapper.readValue(serialized, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
