package dev.senna.controller;

import dev.senna.controller.dto.request.AddItemRequestDto;
import dev.senna.controller.dto.request.AssignOrderToItemRequestDto;
import dev.senna.controller.dto.request.UpdateItemRequestDto;
import dev.senna.model.enums.ItemStatus;
import dev.senna.service.ItemService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;

@Path("/api/item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemController {

    @Inject
    private ItemService itemService;

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);


    @POST
    @Transactional
    public Response createItem( @Valid  AddItemRequestDto reqDto) {

        log.debug("Received request do create an item");
        var itemId = itemService.addItem(reqDto);
        log.info("Item created with success! ID: {}", itemId);
        return Response.created(URI.create("/item/" + itemId)).build();

    }

    @GET
    @Path("/{itemId}")
    public Response findItemById(@PathParam("itemId") Long itemId) {
        log.debug("Received request find item by id {}", itemId);
        return Response.ok(itemService.findItemById(itemId)).build();
    }

    @PATCH
    @Path("/{itemId}/order")
    @Transactional
    public Response assignOrder( @PathParam("itemId") Long itemId, @Valid AssignOrderToItemRequestDto reqDto ) {
        log.debug("Received request assign order to an item {}", itemId);
        itemService.assignOrder(reqDto, itemId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PATCH
    @Path("/{itemId}")
    @Transactional
    public Response updateItem(@PathParam("itemId") Long itemId, @Valid UpdateItemRequestDto reqDto) {
        log.info("Received request update an item {}", itemId);
        itemService.updateItem(itemId, reqDto);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/search")
    @Operation(
            summary = "Search for items by status",
            description = "Returns a list of items that match the given status filter."
    )
    @APIResponse(
            responseCode = "200",
            description = "List of items found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AbstractReadWriteAccess.Item.class, type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid input, e.g., missing or unknown status"
    )
    public Response searchItemByStatus(
            @QueryParam("status") @NotNull(message = "Query param 'status' should not be null") ItemStatus status
            ) {
        log.debug("Received request search item by status {}" , status.name());
        var items = itemService.findByStatus(status);
        return Response.status(Response.Status.OK).entity(items).build();
    }
}
