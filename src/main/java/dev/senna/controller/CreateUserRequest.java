package dev.senna.controller;

public record CreateUserRequest(
        String username,
        String password
) {
}
