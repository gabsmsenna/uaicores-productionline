package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

public class UserNotFoundException extends HttpProblem  {

    public UserNotFoundException() {
        super(builder()
                .withTitle("User not found")
                .withStatus(Response.Status.NOT_FOUND)
                .withDetail("User with username not found in the system")
                .with("timestamp", LocalDateTime.now().toString()));
    }
}
