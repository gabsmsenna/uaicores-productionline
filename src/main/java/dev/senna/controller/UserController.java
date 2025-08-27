package dev.senna.controller;

import dev.senna.controller.dto.request.CreateUserRequest;
import dev.senna.controller.dto.request.UpdateUserDto;
import dev.senna.service.ClientService;
import dev.senna.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

@Path("/user")
public class UserController {

    @Inject
    private UserService userService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @RolesAllowed("ADMIN")
    public Response createUser(CreateUserRequest createUserRequest) {

        log.debug("Received request to create a new user. Data: {} ", createUserRequest);

        var userId = userService.createUser(createUserRequest);

        log.info("User created successfully - ID: {}", userId);

        return Response.created(URI.create("/user/" + userId)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("ADMIN")
    public Response findAllUsers(@QueryParam("page") @DefaultValue("0") Integer page,
                                 @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        log.debug("Received request to list users. Page: {}, Page size: {}", page, pageSize);
        var users = userService.findAll(page, pageSize);
        log.info("Users listed successfully. Returning {} users to page {}", users.size(), page);
        return Response.ok(users).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response getUserById(@PathParam("id") UUID userId) {

        log.debug("Received request to find user by id {}", userId);
        var user = userService.findUserById(userId);
        log.info("User found successfully! Returning the user...");
        return Response.ok(user).build();
    }

    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response updateUser(@PathParam("id") UUID userId, UpdateUserDto createUserRequest) {

        log.debug("Received request to update user with id {}", userId);
        userService.updateUser(userId, createUserRequest);
        log.info("User updated successfully! Returning 204 no content");
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response deleteUser(@PathParam("id") UUID userId) {

        log.debug("Received request to delete user with id {}", userId);
        userService.deleteUser(userId);
        log.info("User deleted successfully! Returning 204 no content");
        return Response.noContent().build();
    }
}
