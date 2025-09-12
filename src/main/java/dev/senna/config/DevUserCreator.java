package dev.senna.config;

import dev.senna.controller.dto.request.CreateUserRequest;
import dev.senna.model.enums.UserRole;
import dev.senna.repository.UserRepository;
import dev.senna.service.UserService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DevUserCreator {

    private static final Logger log = LoggerFactory.getLogger(DevUserCreator.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @ConfigProperty(name = "dev.user.name")
    String devUsername;

    @ConfigProperty(name = "dev.user.password")
    String devPassword;

    @Transactional
    public void createDevUserOnStartup(@Observes StartupEvent event) {
        log.info("Checking for DEV user on startup...");

        if (!userRepository.existsByRole(UserRole.DEV)) {
            log.info("No DEV user found. Creating one...");

            var devUserRequest = new CreateUserRequest(
                    devUsername,
                    devPassword,
                    UserRole.DEV
            );

            try {
                userService.createUserBySystem(devUserRequest);
                log.info("DEV user created successfully!");
            } catch (Exception e) {
                log.error("Failed to create DEV user", e);
            }
        } else {
            log.info("DEV user already exists.");
        }
    }
}
