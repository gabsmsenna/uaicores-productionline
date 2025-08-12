package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;

import java.time.LocalDateTime;
import java.util.UUID;

public class ClientAlreadyExistsException extends HttpProblem {
    public ClientAlreadyExistsException(String clientName) {
        super(builder()
                .withDetail("Client (" + clientName + ") already exists on database")
                .withStatus(400)
                .withTitle("Client already exists")
                .with("timestamp", LocalDateTime.now().toString()));

    }
}
