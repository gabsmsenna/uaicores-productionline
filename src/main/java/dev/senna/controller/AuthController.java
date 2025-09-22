package dev.senna.controller;

import dev.senna.controller.dto.request.LoginRequestDTO;
import dev.senna.controller.dto.request.RefreshRequest;
import dev.senna.controller.dto.response.TokenResponse;
import dev.senna.infra.JwtService;
import dev.senna.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    UserRepository userRepository;
    @Inject
    JwtService jwtService;

    @POST
    @Path("/login")
    @PermitAll
    @Transactional
    public Response login(LoginRequestDTO loginCredentials) {
        var userOpt = userRepository.findByUsername(loginCredentials.username());
        if (userOpt.isEmpty()) return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();

        var userEntity = userOpt.get();
        if (!BcryptUtil.matches(loginCredentials.plainTextPasswd(), userEntity.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }

        String access = jwtService.generateAccessToken(userEntity);
        String refresh = jwtService.generateAndStoreRefreshToken(userEntity);

        return Response.ok(new TokenResponse(access, jwtService.accessTokenTtlSeconds(), refresh, userEntity.getRole())).build();
    }

    @POST
    @Path("/token/refresh")
    @PermitAll
    @Transactional
    public Response refresh(RefreshRequest refreshRequest) {
        var userOpt = jwtService.rotateRefreshToken(refreshRequest.refreshToken());
        if (userOpt.isEmpty()) return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid refresh token").build();

        var user =  userOpt.get();
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateAndStoreRefreshToken(user);

        return Response.ok(new TokenResponse(access, jwtService.accessTokenTtlSeconds(), refresh, user.getRole())).build();
    }
}
