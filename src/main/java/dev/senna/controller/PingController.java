package dev.senna.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/ping")
@Produces(MediaType.APPLICATION_JSON)
public class PingController {

    @GET
    public Response ping() {

        return Response.ok().entity("pong").build();
    }
}
