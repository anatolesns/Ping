package fr.epita.assistants.ping.presentation.rest;

import fr.epita.assistants.ping.data.model.UserModel;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import fr.epita.assistants.ping.utils.HashUtils;
import fr.epita.assistants.ping.utils.ErrorInfo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@Path("/api/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    @Inject
    EntityManager entityManager;


    public static class LoginRequest {
        public String login;
        public String password;
    }


    private String HMAC_KEY = "une_cle_secrete_tres_longue_et_securisee_pour_ping_2026";
    @POST
    @Path("/login")
    @Transactional
    public Response login(LoginRequest request) {
        if (is_valid(request.login) == false) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Invalid login format\"}")
                    .build();
        }

        try {
            UserModel user = entityManager.createQuery(
                    "SELECT u FROM UserModel u WHERE u.login = :login", UserModel.class)
                    .setParameter("login", request.login)
                    .getSingleResult();

            String decryptedPassword = HashUtils.decrypt(user.getPassword());
            if (!decryptedPassword.equals(request.password)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Wrong password\"}")
                        .build();
            }
            Set<String> roles = new HashSet<>();
            if (user.getIsAdmin() != null && user.getIsAdmin()) {
                roles.add("admin");
            } else {
                roles.add("user");
            }

            String token = Jwt.subject(user.getId().toString())
                .groups(roles)
                .issuedAt(Instant.now().getEpochSecond())
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond())
                .signWithSecret(HMAC_KEY);

            return Response.ok("{\"token\": \"" + token + "\"}").build();

        } catch (NoResultException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"User not found\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Authentication failed\"}")
                    .build();
        }
    }

    private boolean is_valid(String login) {
        if (login == null) {
            return false;
        }
        int d = 0;
        int u = 0;
        for (int i = 0; i < login.length(); i++) {
            if (login.charAt(i) == '.') 
            {
                d++;
            }
            if (login.charAt(i) == '_') 
            {
                u++;
            }
        }
        if ((d == 1 && u == 0) || (u == 1 && d == 0)) {
            return true;
        }
        return false;
    }

}
