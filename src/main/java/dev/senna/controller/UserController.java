package dev.senna.controller;

import dev.senna.controller.dto.request.CreateUserRequest;
import dev.senna.controller.dto.request.UpdateUserDto;
import dev.senna.service.UserService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/user")
public class UserController {

    @Inject
    private UserService userService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createUser(CreateUserRequest createUserRequest) {

        var userId = userService.createUser(createUserRequest);

        return Response.created(URI.create("/user/" + userId)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAllUsers(@QueryParam("page") @DefaultValue("0") Integer page,
                                 @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        var users = userService.findAll(page, pageSize);

        return Response.ok(users).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response getUserById(@PathParam("id") UUID userId) {

        return Response.ok(userService.findUserById(userId)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Transactional
    public Response updateUser(@PathParam("id") UUID userId, UpdateUserDto createUserRequest) {

        return Response.ok(userService.updateUser(userId, createUserRequest)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") UUID userId) {

        userService.deleteUser(userId);

        return Response.noContent().build();
    }
}
