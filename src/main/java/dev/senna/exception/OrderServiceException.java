package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

public class OrderServiceException extends HttpProblem {

    public OrderServiceException(String msg, Exception e) {
        super(builder()
                .withTitle("Order Service Exception")
                .withStatus(Response.Status.INTERNAL_SERVER_ERROR)
                .withDetail("Failed to create a new order")
                .with("timestamp", LocalDateTime.now().toString()));
    }
}
