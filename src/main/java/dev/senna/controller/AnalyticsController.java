package dev.senna.controller;

import dev.senna.service.AnalyticsService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/analytics")
public class AnalyticsController {

    @Inject
    AnalyticsService analyticsService;

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);


    @GET
    @Path("/dashboard")
    @RolesAllowed({"ADMIN","DEV", "OFFICER"})
    public Response getDashboardAnalytics() {

        log.debug("Received request to get dashboard analytics");
        var analytics = analyticsService.getDashboardAnalyticsService();
        return Response.ok(analytics).build();
    }
}
