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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Path("/order")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderController {

    @Inject
    private OrderService orderService;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @POST
    @Transactional
    public Response createOrder(@Valid CreateOrderReqDto reqDto) {

        try {
            var orderId = orderService.createOrder(reqDto);
            return Response.created(URI.create("/order/" + orderId)).build();
        } catch (Exception e) {
            log.error("API error during the createOrder: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listOrders( @QueryParam("page") @DefaultValue("0") Integer page,
                                @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        try {
            var orders = orderService.listOrders(page, pageSize);
            return Response.ok(orders).build();
        } catch (Exception e) {
            log.error("API error during the listOrders: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/production")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listOrdersProduction(@QueryParam("page") @DefaultValue("0") Integer page,
                                         @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        try {
            var ordersInProduction = orderService.listOrdersInProduction(page, pageSize);
            return Response.ok(ordersInProduction).build();
        } catch (Exception e) {
            log.error("API error during the listOrdersInProduction: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/last-send-orders")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listLastSendOrders(@QueryParam("page") @DefaultValue("0") Integer page,
                                       @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        try {
            var lastSendOrders = orderService.listLastSendOrders(page, pageSize);
            return Response.ok(lastSendOrders).build();
        } catch (Exception e) {
            log.error("API error during the listLastSendOrders: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{orderId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateOrder(@PathParam("orderId") Long orderId, @Valid UpdateOrderReqDto reqDto) {
        log.debug("Received the request to update an order");

        try {
            return Response.ok(orderService.updateOrder(orderId, reqDto)).build();
        } catch (Exception e) {
            log.error("API error during the updateOrder: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/order-statistics")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrderStatistics() {
        log.debug("Received the request to generate order statistics");

        try {
            var stats = orderService.getOrderStatistics();
            return Response.ok(stats).build();
        } catch (Exception e) {
            log.error("API error during the statistics generation: {} ",  e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
