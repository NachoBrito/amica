package es.nachobrito.amica.domain.model.message;

/**
 * @author nacho
 */
public record Message<PAYLOAD extends MessagePayload>(
        MessageId id,
        ConversationId conversationId,
        MessageTopic topic,
        PAYLOAD payload
) {}
