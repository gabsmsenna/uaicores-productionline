package dev.senna.controller;

import dev.senna.controller.dto.request.CreateClientReqDto;
import dev.senna.controller.dto.request.UpdateClientReqDto;
import dev.senna.service.ClientService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

@Path("/api/client")
public class ClientController {

    @Inject
    ClientService clientService;

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    @POST
    @Transactional
    @RolesAllowed({"ADMIN", "DEV"})
    public Response createClient(CreateClientReqDto reqDto) {
            log.debug("Received the request to create a client");
            var clientId = clientService.createClient(reqDto);
            return Response.created(URI.create("/client/" + clientId)).build();
    }

    @GET
    @RolesAllowed({"ADMIN","DEV", "OFFICER"})
    public Response findAllClients(@QueryParam("page") @DefaultValue("0") Integer page,
                                   @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

            log.debug("Received the request to list all clients");
            var clients = clientService.findAllClients(page, pageSize);
            return Response.ok(clients).build();
    }

    @GET
    @Path("/{clientId}")
    @RolesAllowed({"ADMIN","DEV", "OFFICER"})
    public Response listClientById(@PathParam("clientId") UUID clientId) {

            log.debug("Received the request to list a specific client");
            return Response.ok(clientService.findClientById(clientId)).build();
    }

    @PUT
    @Path("/{clientId}")
    @RolesAllowed({"ADMIN", "DEV"})
    public Response updateClient(@PathParam("clientId") UUID clientId, @Valid UpdateClientReqDto reqDto) {

            var client = clientService.updateClient(clientId, reqDto);
            return Response.ok(client).build();
    }

    @DELETE
    @Path("/{clientId}")
    @RolesAllowed({"ADMIN", "DEV"})
    public Response deleteClient(@PathParam("clientId") UUID clientId) {

            log.debug("Received the request to delete a client");
            clientService.deleteClient(clientId);
            return Response.noContent().build();
    }
}
