package dev.senna.controller;

import dev.senna.service.UserService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

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

}
