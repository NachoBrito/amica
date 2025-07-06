package es.nachobrito.amica.domain.model.message;

import java.util.UUID;

/**
 * @author nacho
 */
public record ConversationId(String value) {

    public static ConversationId newRandom(){
        return new ConversationId(UUID.randomUUID().toString());
    }
}
