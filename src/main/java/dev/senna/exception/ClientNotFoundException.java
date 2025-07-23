package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;

import java.time.LocalDateTime;
import java.util.UUID;

public class ClientNotFoundException extends HttpProblem {

    public ClientNotFoundException(UUID clientId) {
        super(builder()
                .withDetail("Client with id " + clientId + " not found")
                .withStatus(404)
                .withTitle("Client not found")
                .with("timestamp", LocalDateTime.now().toString()));

    }
}
