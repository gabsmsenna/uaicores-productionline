package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserNotFoundException extends HttpProblem  {

    public UserNotFoundException(UUID userId) {
        super(builder()
                .withTitle("User not found")
                .withStatus(Response.Status.NOT_FOUND)
                .withDetail("User not found on the system! UserID : " + userId )
                .with("timestamp", LocalDateTime.now().toString()));
    }
}
