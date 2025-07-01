package dev.senna.controller;

import dev.senna.controller.dto.request.CreateClientReqDto;
import dev.senna.service.ClientService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/client")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClientController {

    @Inject
    private ClientService clientService;

    @POST
    @Transactional
    public Response createClient(CreateClientReqDto reqDto) {

        var clientId = clientService.createClient(reqDto);

        return Response.created(URI.create("/client/" + clientId)).build();
    }
}
