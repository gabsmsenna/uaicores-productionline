package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;

import java.time.LocalDate;

public class InvalidEditOrderStatusParameterException extends HttpProblem {
    public InvalidEditOrderStatusParameterException(String msg) {
        super(builder()
                .withDetail("The parameter passed for order editing is invalid")
                .withStatus(400)
                .withTitle("Editing parameter is not valid!")
                .with("timestamp", LocalDate.now().toString()));
    }
}
