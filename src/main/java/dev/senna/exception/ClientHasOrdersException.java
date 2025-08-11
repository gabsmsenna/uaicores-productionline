package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.UUID;

public class ClientHasOrdersException extends HttpProblem {
    public ClientHasOrdersException(UUID clientId, String clientName) {
        super(builder()
                .withDetail("It's not possible deleting the client (" + clientName + ") with id " + clientId + " because it has orders associated")
                .with("clientName", clientName)
                .with("clientId", clientId)
                .withStatus(Response.Status.CONFLICT)
                .withTitle("Client has orders associated")
                .with("timestamp", LocalDateTime.now().toString()));
    }
}
