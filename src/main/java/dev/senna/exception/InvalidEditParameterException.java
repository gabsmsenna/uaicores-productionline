package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;

import java.time.LocalDateTime;

public class InvalidEditParameterException extends HttpProblem {

    public InvalidEditParameterException() {
        super(builder()
                .withTitle("Invalid Edit Parameters")
                .withStatus(400)
                .withDetail("No valid parameters were provided for the update request.")
                .with("timestamp", LocalDateTime.now().toString()));
    }
}