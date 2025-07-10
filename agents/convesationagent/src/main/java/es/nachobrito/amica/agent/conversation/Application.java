package es.nachobrito.amica.agent.conversation;

import es.nachobrito.amica.domain.model.message.payload.UserRequest;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.annotation.SerdeImport;

@SerdeImport(UserRequest.class)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}