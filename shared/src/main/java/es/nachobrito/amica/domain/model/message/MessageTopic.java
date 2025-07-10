package es.nachobrito.amica.domain.model.message;

/**
 * @author nacho
 */
public record MessageTopic(String name) {
    public static final MessageTopic USER_REQUESTS = new MessageTopic("es.nachobrito.amica.user.request.v1");
}
