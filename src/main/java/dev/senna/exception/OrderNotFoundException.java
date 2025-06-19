package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;

import java.time.LocalDateTime;

public class OrderNotFoundException extends HttpProblem {

    public OrderNotFoundException(Long id) {
        super(builder()
                .withTitle("Order not found")
                .withStatus(404)
                .withDetail("Order with id " + id + " not found on the application")
                .with("timestamp", LocalDateTime.now().toString()));
    }
}
