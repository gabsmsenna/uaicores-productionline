package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

public class ItemAlreadyHasOrder extends HttpProblem {

    public ItemAlreadyHasOrder() {
        super(builder()
                .withTitle("Item already has order")
                .withStatus(Response.Status.CONFLICT)
                .withDetail("This item already has a order associted with")
                .with("timestamp", LocalDateTime.now().toString()));
    }
}
