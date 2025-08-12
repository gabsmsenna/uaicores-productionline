package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

public class UserAlreadyExistsException extends HttpProblem {

    public UserAlreadyExistsException(String username) {
        super(builder()
                .withTitle("User already exists")
                .withStatus(Response.Status.CONFLICT)
                .withDetail("User with username " + username + " already exists in the system")
                .with("username", username)
                .with("timestamp", LocalDateTime.now().toString())
        );
    }
}
