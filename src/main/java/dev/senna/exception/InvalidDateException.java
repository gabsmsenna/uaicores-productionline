package dev.senna.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;

import java.time.LocalDate;

public class InvalidDateException extends HttpProblem {

    public InvalidDateException(String msg, LocalDate date) {
        super(builder()
                .withDetail("The specified date is not valid for this operation. " + date )
                .withStatus(400)
                .withTitle("Date is invalid!")
                .with("timestamp", LocalDate.now().toString()));
    }
}
