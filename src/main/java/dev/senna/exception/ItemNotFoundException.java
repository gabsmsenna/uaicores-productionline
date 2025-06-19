package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;

import java.time.LocalDateTime;

public class ItemNotFoundException extends HttpProblem {

    public ItemNotFoundException(Long id) {
        super(builder()
                .withTitle("Item Not Found ")
                .withStatus(404)
                .withDetail("Item with ID " + id + " not found on the application")
                .with("timestamp", LocalDateTime.now().toString()));
    }
}
