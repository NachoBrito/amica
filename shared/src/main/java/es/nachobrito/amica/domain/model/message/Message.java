package es.nachobrito.amica.domain.model.message;

/**
 * @author nacho
 */
public record Message<P extends MessagePayload>(
        MessageId id,
        ConversationId conversationId,
        MessageTopic topic,
        P payload
) {}
