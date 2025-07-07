package es.nachobrito.amica.domain.model.message;

/**
 * @author nacho
 */
public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException(String s) {
        super(s);
    }
}
