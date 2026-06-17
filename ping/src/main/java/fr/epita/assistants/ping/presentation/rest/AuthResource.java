package fr.epita.assistants.ping.presentation.rest;

import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.request.LoginRequest;
import fr.epita.assistants.request.NewUserRequest;
import fr.epita.assistants.response.UserResponse;
import fr.epita.assistants.response.UserResponseList;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import fr.epita.assistants.ping.utils.HashUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Path("/api/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    @Inject
    EntityManager entityManager;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/all")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        List<UserModel> all = entityManager
                .createQuery("SELECT u FROM UserModel u", UserModel.class)
                .getResultList();

        List<UserResponseList> res = new ArrayList<>();

        for (UserModel user : all) {
            UserResponseList dto = new UserResponseList();
            dto.id = user.getId();
            dto.login = user.getLogin();
            dto.displayName = user.getDisplayName();
            dto.isAdmin = user.getIsAdmin();
            dto.avatar = user.getAvatar();
            res.add(dto);
        }

        return Response.ok(res).build();
    }

    @POST
    @Transactional
    @RolesAllowed("admin")
    public Response createUser(NewUserRequest request) {

        if (request.login == null || request.password == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The login or the password is invalid\"}")
                    .build();
        }
        if (is_valid(request.login) == false) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Invalid login format\"}")
                    .build();
        }

        long nb = entityManager.createQuery(
                "SELECT COUNT(u) FROM UserModel u WHERE u.login = :login", Long.class)
                .setParameter("login", request.login)
                .getSingleResult();
        if (nb > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"The login is already taken\"}")
                    .build();
        }

        String separator;

        if (request.login.contains(".")) {
            separator = "\\.";
        } else {
            separator = "_";
        }
        String[] tab = request.login.split(separator);
        StringBuilder aux = new StringBuilder();

        for (String part : tab) {
            if (!part.isEmpty()) {
            
                if (aux.length() > 0) {
                    aux.append(" ");
                }
                String temp = Character.toUpperCase(part.charAt(0)) + part.substring(1);
                aux.append(temp);
            }
        }

        String Name = aux.toString();

        String password;
        try {
            password = HashUtils.encrypt(request.password);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"erorr encrypt\"}")
                    .build();
        }

        boolean admin = false;

        if (request.isAdmin != null && request.isAdmin) {
            admin = true;
            
        }

        UserModel user = new UserModel(request.login, password, Name, null, admin);
        entityManager.persist(user);

        return Response.ok(new UserResponse(user)).build();
    }

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

            String password = HashUtils.decrypt(user.getPassword());
            if (!password.equals(request.password)) {
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
            String token = Jwt.claims()
            .issuer("Groupe7")
                .subject(user.getId().toString())
                .groups(roles)
                .issuedAt(Instant.now().getEpochSecond())
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond())
                .sign();

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

    @GET
    @Path("/refresh")
    @RolesAllowed({"user", "admin"})
    public Response refreshToken() {
        String subject = jwt.getSubject();
        try {
            UUID userId = UUID.fromString(subject);
            UserModel user = entityManager.find(UserModel.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"The user could not be found\"}")
                        .build();
            }
            Set<String> roles = new HashSet<>();
            if (user.getIsAdmin() != null && user.getIsAdmin()) {
                roles.add("admin");
            } else {
                roles.add("user");
            }
            String token = Jwt.claims()
                    .issuer("Groupe7")
                    .subject(user.getId().toString())
                    .groups(roles)
                    .issuedAt(Instant.now().getEpochSecond())
                    .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond())
                    .sign();
            return Response.ok("{\"token\": \"" + token + "\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"error\"}")
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
