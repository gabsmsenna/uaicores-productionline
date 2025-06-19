package dev.senna.controller;

import dev.senna.controller.dto.CreateOrderReqDto;
import dev.senna.service.OrderService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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
}
