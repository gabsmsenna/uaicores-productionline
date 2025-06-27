package dev.senna.controller;

import dev.senna.controller.dto.request.AddItemRequestDto;
import dev.senna.controller.dto.request.AssignOrderToItemRequestDto;
import dev.senna.controller.dto.request.UpdateItemRequestDto;
import dev.senna.service.ItemService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemController {

    @Inject
    private ItemService itemService;

    @POST
    @Transactional
    public Response createItem( @Valid  AddItemRequestDto reqDto) {

        var itemId = itemService.addItem(reqDto);

        return Response.status(Response.Status.CREATED).entity(itemId).build();

    }

    @GET()
    @Path("/production")
    public Response listProductionLine ( @QueryParam("page") @DefaultValue("0") Integer page,
                                         @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        var producionLine = itemService.listProduction(page, pageSize);

        return Response.status(Response.Status.OK).entity(producionLine).build();
    }

    @PUT
    @Path("/{itemId}/order")
    @Transactional
    public Response assignOrder( @PathParam("itemId") Long itemId, @Valid AssignOrderToItemRequestDto reqDto ) {

        itemService.assignOrder(reqDto, itemId);

        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{itemId}")
    @Transactional
    public Response updateItem(@PathParam("itemId") Long itemId, @Valid UpdateItemRequestDto reqDto) {
        itemService.updateItem(itemId, reqDto);
        return Response.status(Response.Status.OK).build();
    }
}
