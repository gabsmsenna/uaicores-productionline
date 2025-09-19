package dev.senna.controller;

import dev.senna.service.ItemService;
import dev.senna.service.OrderService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jaxb.core.v2.TODO;

@Path("/api/production")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN","DEV", "OFFICER"})
public class ProductionController {

    @Inject
    private ItemService itemService;

    @Inject
    private OrderService orderService;

    @GET()
    @Path("/items")
    public Response listItemsProductionLine(@QueryParam("page") @DefaultValue("0") Integer page,
                                            @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        var producionLine = itemService.listProduction(page, pageSize);

        return Response.status(Response.Status.OK).entity(producionLine).build();
    }

    @GET()
    @Path("/orders")
    public Response listOrderProductionLine(@QueryParam("page") @DefaultValue("0") Integer page,
                                            @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        var orderProductionLine = orderService.listProduction(page, pageSize);

        return Response.status(Response.Status.OK).entity(orderProductionLine).build();
    }

    // todo Criar endpoint com produtos aguardando envio
}
