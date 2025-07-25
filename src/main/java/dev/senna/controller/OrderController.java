package dev.senna.controller;

import dev.senna.controller.dto.request.CreateOrderReqDto;
import dev.senna.controller.dto.request.UpdateOrderReqDto;
import dev.senna.service.OrderService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/order")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderController {

    @Inject
    private OrderService orderService;

    @POST
    @Transactional
    public Response createOrder(@Valid CreateOrderReqDto reqDto) {

        var orderCreated = orderService.createOrder(reqDto);

        return Response.created(URI.create("/order/" + orderCreated.getId())).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listOrders( @QueryParam("page") @DefaultValue("0") Integer page,
                                @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        var orders = orderService.listOrders(page, pageSize);

        return Response.ok(orders).build();
    }

    @GET
    @Path("/production")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listOrdersProduction(@QueryParam("page") @DefaultValue("0") Integer page,
                                         @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        var ordersInProduction = orderService.listOrdersInProduction(page, pageSize);

        return Response.ok(ordersInProduction).build();
    }

    @GET
    @Path("/last-send-orders")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listLastSendOrders(@QueryParam("page") @DefaultValue("0") Integer page,
                                       @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        var lasSendOrders = orderService.listLastSendOrders(page, pageSize);

        return Response.ok(lasSendOrders).build();
    }

    @PUT
    @Path("/{orderId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateOrder(@PathParam("orderId") Long orderId, @Valid UpdateOrderReqDto reqDto) {

        return Response.ok(orderService.updateOrder(orderId, reqDto)).build();
    }
}
