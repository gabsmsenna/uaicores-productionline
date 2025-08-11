package dev.senna.controller;

import dev.senna.controller.dto.request.CreateClientReqDto;
import dev.senna.controller.dto.request.UpdateClientReqDto;
import dev.senna.service.ClientService;
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

@Path("/client")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClientController {

    @Inject
    ClientService clientService;

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    @POST
    @Transactional
    public Response createClient(CreateClientReqDto reqDto) {

        try {
            log.debug("Received the request to create a client");
            var clientId = clientService.createClient(reqDto);
            return Response.created(URI.create("/client/" + clientId)).build();
        } catch (RuntimeException e) {
            log.error("API error during the createClient: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    public Response findAllClients(@QueryParam("page") @DefaultValue("0") Integer page,
                                   @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        try {
            log.debug("Received the request to list all clients");
            var clients = clientService.findAllClients(page, pageSize);
            return Response.ok(clients).build();
        } catch (RuntimeException e) {
            log.error("API error during the findAllClients: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{clientId}")
    public Response listClientById(@PathParam("clientId") UUID clientId) {

        try {
            log.debug("Received the request to list a specific client");
            return Response.ok(clientService.findClientById(clientId)).build();
        } catch (RuntimeException e) {
            log.error("API error during the listClientById: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{clientId}")
    public Response updateClient(@PathParam("clientId") UUID clientId, @Valid UpdateClientReqDto reqDto) {

        try {
            var client = clientService.updateClient(clientId, reqDto);
            return Response.ok(client).build();
        } catch (RuntimeException e) {
            log.error("API error during the updateClient: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{clientId}")
    public Response deleteClient(@PathParam("clientId") UUID clientId) {

        try {
            clientService.deleteClient(clientId);
            return Response.noContent().build();
        } catch (RuntimeException e) {
            log.error("API error during the deleteClient: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
