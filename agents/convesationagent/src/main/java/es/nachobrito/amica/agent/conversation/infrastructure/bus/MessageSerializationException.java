package es.nachobrito.amica.agent.conversation.infrastructure.bus;

import java.io.IOException;

/**
 * @author nacho
 */
public class MessageSerializationException extends RuntimeException{
    public MessageSerializationException(String error, IOException cause) {
        super(error, cause);
    }
}
