package es.nachobrito.amica.domain.model.message;

import java.util.UUID;

/**
 * @author nacho
 */
public record MessageId(String value) {

    public static MessageId newRandom(){
        return new MessageId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return "Message/%s".formatted(value);
    }
}
