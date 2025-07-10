package es.nachobrito.amica.agent.conversation.infrastructure.bus;

import es.nachobrito.amica.domain.model.message.MessageBus;
import es.nachobrito.amica.infrastructure.hivemqtt.HiveMqttMessageBus;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

/**
 * @author nacho
 */
@Factory
public class MessageBusFactory {
    @Singleton
    MessageBus messageBus(
            @Value("${amica.mqtt.host}") String host,
            @Value("${amica.mqtt.client.identifier}") String identifier,
            SerdePayloadSerializer payloadSerializer
    ){
        return new HiveMqttMessageBus(host, identifier, payloadSerializer);
    }
}
