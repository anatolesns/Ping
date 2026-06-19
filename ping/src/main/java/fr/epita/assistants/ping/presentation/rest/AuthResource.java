package fr.epita.assistants.ping.presentation.rest;

import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.ping.data.repository.UserRepository;
import fr.epita.assistants.ping.request.LoginRequest;
import fr.epita.assistants.ping.request.NewUserRequest;
import fr.epita.assistants.ping.request.UpdateUserRequest;
import fr.epita.assistants.ping.response.UserResponse;
import fr.epita.assistants.ping.response.UserResponseList;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/all")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        List<UserModel> all = userRepository.findAll();

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

        if (userRepository.loginExists(request.login)) {
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
        userRepository.save(user);

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
            UserModel user = userRepository.findByLogin(request.login);

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
            return Response.status(Response.Status.UNAUTHORIZED)
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
            UserModel user = userRepository.findById(userId);
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
        if (login == null || login.isBlank()) {
            return false;
        }
        int d = 0;
        int u = 0;
        for (int i = 0; i < login.length(); i++) {
            if (login.charAt(i) == '.') 
            {
                if (i==0 || i == login.length()-1){
                    return false;
                }
                d++;
            }
            if (login.charAt(i) == '_') 
            {
                if (i==0 || i == login.length()-1){
                    return false;
                }
                u++;
            }
        }
        if ((d == 1 && u == 0) || (u == 1 && d == 0)) {
            return true;
        }
        return false;
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Response getUser(@PathParam("id") UUID id) {
        UserModel user = userRepository.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"User not found\"}")
                    .build();
        }

        String call = jwt.getSubject();
        boolean isAdmin = jwt.getGroups().contains("admin");
        if (!isAdmin && !id.toString().equals(call)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"The user is not allowed to access this user\"}")
                    .build();
        }

        return Response.ok(new UserResponse(user)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"user", "admin"})
    public Response updateUser(@PathParam("id") UUID id, UpdateUserRequest request) {
        UserModel user = userRepository.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The user could not be found\"}")
                    .build();
        }

        String call = jwt.getSubject();
        boolean isAdmin = jwt.getGroups().contains("admin");
        if (!isAdmin && !id.toString().equals(call)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"The user is not allowed\"}")
                    .build();
        }

        if (request.displayName != null && request.displayName.isBlank() == false) {
                    user.setDisplayName(request.displayName);
                }

        if (request.password != null && request.password.isBlank() == false) {
            try {
                user.setPassword(HashUtils.encrypt(request.password));
            } catch (Exception e) {
                return Response.status(401)
                        .entity("{\"message\": \"Error password\"}")
                        .build();
            }
        }
        
        if (request.avatar != null) {
            user.setAvatar(request.avatar);
        }

        return Response.ok(new UserResponse(user)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("admin")
    public Response deleteUser(@PathParam("id") UUID id) {
        UserModel user = userRepository.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The user could not be found\"}")
                    .build();
        }

        userRepository.delete(user);

        return Response.noContent().build();
    }

    

}
