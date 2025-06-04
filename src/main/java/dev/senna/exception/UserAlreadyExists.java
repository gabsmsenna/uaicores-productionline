package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserAlreadyExists extends HttpProblem {

    public UserAlreadyExists(String username) {
        super(builder()
                .withTitle("User already exists")
                .withStatus(Response.Status.CONFLICT)
                .withDetail("User with username " + username + " already exists in the system")
                .with("username", username)
                .with("timestamp", LocalDateTime.now().toString())
        );
    }
}
