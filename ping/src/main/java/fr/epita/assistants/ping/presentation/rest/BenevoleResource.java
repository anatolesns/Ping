package fr.epita.assistants.ping.presentation.rest;

import fr.epita.assistants.ping.data.model.BenevoleModel;
import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.ping.data.repository.BenevoleRepository;
import fr.epita.assistants.ping.data.repository.UserRepository;
import fr.epita.assistants.ping.request.UpdateBenevoleRequest;
import fr.epita.assistants.ping.response.BenevoleResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import fr.epita.assistants.ping.utils.EmailUtils;
import fr.epita.assistants.ping.utils.HashUtils;
import fr.epita.assistants.ping.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/api/benevole")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BenevoleResource {

    @Inject
    BenevoleRepository benevoleRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    Logger logger;

    @GET
    @Path("/me")
    @RolesAllowed({"user", "admin"})
    public Response getMyBenevole() {
        UUID userId = UUID.fromString(jwt.getSubject());
        BenevoleModel benevole = benevoleRepository.findById(userId);
        if (benevole == null) {
            logger.ErrorLog("GET /api/benevole/me, Id = " + jwt.getSubject() + ", benevole not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Benevole profile not found\"}")
                    .build();
        }
        logger.Log("GET /api/benevole/me, Id = " + jwt.getSubject());
        return Response.ok(new BenevoleResponse(benevole)).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response getBenevole(@PathParam("id") UUID id) {
        BenevoleModel benevole = benevoleRepository.findById(id);
        if (benevole == null) {
            logger.ErrorLog("GET /api/benevole/" + id + ", Id = " + jwt.getSubject() + ", benevole not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Benevole not found\"}")
                    .build();
        }
        logger.Log("GET /api/benevole/" + id + ", Id = " + jwt.getSubject());
        return Response.ok(new BenevoleResponse(benevole)).build();
    }

    @GET
    @Path("/all")
    @RolesAllowed("admin")
    public Response getAllBenevoles() {
        List<BenevoleModel> all = benevoleRepository.findAll();
        List<BenevoleResponse> res = new ArrayList<>();
        for (BenevoleModel b : all) {
            res.add(new BenevoleResponse(b));
        }
        logger.Log("GET /api/benevole/all, Id = " + jwt.getSubject());
        return Response.ok(res).build();
    }

    @PUT
    @Path("/me")
    @Transactional
    @RolesAllowed({"user", "admin"})
    public Response updateMyBenevole(UpdateBenevoleRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return upsertBenevole(userId, request);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("admin")
    public Response updateBenevole(@PathParam("id") UUID id, UpdateBenevoleRequest request) {
        return upsertBenevole(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("admin")
    public Response deleteBenevole(@PathParam("id") UUID id) {
        BenevoleModel benevole = benevoleRepository.findById(id);
        if (benevole == null) {
            logger.ErrorLog("DELETE /api/benevole/" + id + ", Id = " + jwt.getSubject() + ", benevole not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Benevole not found\"}")
                    .build();
        }

        benevoleRepository.delete(benevole);
        logger.Log("DELETE /api/benevole/" + id + ", Id = " + jwt.getSubject());
        return Response.noContent().build();
    }

    private Response upsertBenevole(UUID userId, UpdateBenevoleRequest request) {
        UserModel user = userRepository.findById(userId);
        if (user == null) {
            logger.ErrorLog("PUT /api/benevole/" + userId + ", Id = " + jwt.getSubject() + ", user not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"User not found\"}")
                    .build();
        }

        BenevoleModel benevole = benevoleRepository.findById(userId);
        if (benevole == null) {
            benevole = new BenevoleModel(user, request.nomComplet, request.age, request.telephone, request.email);
            if (request.disponibilites != null) benevole.setDisponibilites(request.disponibilites);
            benevoleRepository.save(benevole);
            if (request.email != null && !request.email.isBlank()) {
                sendCredentialsEmail(request.email, user);
            }
        } else {
            if (request.nomComplet != null) benevole.setNomComplet(request.nomComplet);
            if (request.age != null) benevole.setAge(request.age);
            if (request.telephone != null) benevole.setTelephone(request.telephone);
            if (request.email != null) benevole.setEmail(request.email);
            if (request.disponibilites != null) benevole.setDisponibilites(request.disponibilites);
            benevole = benevoleRepository.update(benevole);
        }

        logger.Log("PUT /api/benevole/" + userId + ", Id = " + jwt.getSubject());
        return Response.ok(new BenevoleResponse(benevole)).build();
    }

    private void sendCredentialsEmail(String email, UserModel user) {
        try {
            String plainPassword = HashUtils.decrypt(user.getPassword());
            EmailUtils.sendCredentials(email, user.getLogin(), plainPassword);
            logger.Log("PUT /api/benevole, credentials email sent to " + email + " for login = " + user.getLogin());
        } catch (Exception e) {
            logger.ErrorLog("PUT /api/benevole, send credentials email failed for login = " + user.getLogin() + " , error = " + e.getMessage());
        }
    }
}
