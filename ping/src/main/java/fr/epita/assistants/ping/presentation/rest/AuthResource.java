package fr.epita.assistants.ping.presentation.rest;

import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.ping.data.repository.UserRepository;
import fr.epita.assistants.ping.request.LoginRequest;
import fr.epita.assistants.ping.request.NewUserRequest;
import fr.epita.assistants.ping.request.UpdateUserRequest;
import fr.epita.assistants.ping.response.UserResponse;
import fr.epita.assistants.ping.response.UserResponseList;
import fr.epita.assistants.ping.service.AuthService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import fr.epita.assistants.ping.utils.HashUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import fr.epita.assistants.ping.utils.Logger;

@Path("/api/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    Logger logger;

    @Inject
    AuthService authService;

    /**
     * GET /api/user/all - Retourne la liste de tous les utilisateurs enregistrés.
     * Réservé aux admins et managers.
     */
    @GET
    @Path("/all")
    @RolesAllowed({"admin", "manager"})
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
        logger.Log("GET /api/user/all, Id = " + jwt.getSubject());
        return Response.ok(res).build();
    }

    /**
     * POST /api/user - Crée un nouvel utilisateur (login/mot de passe chiffré) après
     * validation du format de login et de l'unicité du login. Réservé aux admins et managers.
     */
    @POST
    @Transactional
    @RolesAllowed({"admin", "manager"})
    public Response createUser(NewUserRequest request) {

        if (request.login == null || request.password == null) {
            logger.ErrorLog("POST /api/user, Id = " + jwt.getSubject() + " , login or password null");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The login or the password is invalid\"}")
                    .build();
        }
        if (!authService.isValidLogin(request.login)) {
            logger.ErrorLog("POST /api/user, Id = " + jwt.getSubject() + " , invalid login format");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Invalid login format\"}")
                    .build();
        }

        if (userRepository.loginExists(request.login)) {
            logger.ErrorLog("POST /api/user, Id = " + jwt.getSubject() + " , login already taken");
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"The login is already taken\"}")
                    .build();
        }

        String Name = authService.generateDisplayName(request.login);

        String password;
        try {
            password = HashUtils.encrypt(request.password);
        } catch (Exception e) {
            logger.ErrorLog("POST /api/user , Id = " + jwt.getSubject() + " , login = " + request.login + " , encrypt error");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"erorr encrypt\"}")
                    .build();
        }

        boolean admin = false;

        if (request.isAdmin != null && request.isAdmin) {
            admin = true;
        }

        UserModel user = new UserModel(request.login, password, Name, null, admin);

        if (request.role != null && !request.role.isBlank()) {
            user.setRole(request.role);
        }

        userRepository.save(user);
        logger.Log("POST /api/user , Id = " + jwt.getSubject() + " , login = " + request.login + " , isAdmin = " + admin);
        return Response.ok(new UserResponse(user)).build();
    }

    /**
     * POST /api/user/login - Authentifie un utilisateur à partir de son login et mot de
     * passe, et retourne un token JWT en cas de succès.
     */
    @POST
    @Path("/login")
    @Transactional
    public Response login(LoginRequest request) {
        if (!authService.isValidLogin(request.login)) {
            logger.ErrorLog("POST /api/user/login , login = " + request.login + " , invalid login format");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Invalid login format\"}")
                    .build();
        }

        try {
            UserModel user = userRepository.findByLogin(request.login);

            String password = HashUtils.decrypt(user.getPassword());
            if (!password.equals(request.password)) {
                logger.ErrorLog("POST /api/user/login , login = " + request.login + " , wrong password");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Wrong password\"}")
                        .build();
            }
            String token = authService.generateToken(user);

            logger.Log("POST /api/user/login : login = " + request.login + " , userId = " + user.getId());
            return Response.ok("{\"token\": \"" + token + "\"}").build();

        } catch (NoResultException e) {
            logger.ErrorLog("POST /api/user/login , login = " + request.login + " , User not found");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"User not found\"}")
                    .build();
        } catch (Exception e) {
            logger.ErrorLog("POST /api/user/login , login = " + request.login + " , Authentication failed");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Authentication failed\"}")
                    .build();
        }
    }

    /**
     * GET /api/user/refresh - Régénère un nouveau token JWT pour l'utilisateur actuellement
     * authentifié (identifié via le sujet du token en cours).
     */
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
            String token = authService.generateToken(user);
            logger.Log("GET /api/user/refresh , Id = " + subject);
            return Response.ok("{\"token\": \"" + token + "\"}").build();
        } catch (Exception e) {
            logger.ErrorLog("GET /api/user/refresh , Id = " + subject + ", error = " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"error\"}")
                    .build();
        }
    }

    /**
     * GET /api/user/{id} - Retourne les informations d'un utilisateur donné.
     * Un utilisateur non admin ne peut consulter que son propre profil.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Response getUser(@PathParam("id") UUID id) {
        UserModel user = userRepository.findById(id);
        String call = jwt.getSubject();
        if (user == null) {
            logger.ErrorLog("GET /api/user/" + id + " , Id = " + call + " , user not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"User not found\"}")
                    .build();
        }

        boolean isAdmin = jwt.getGroups().contains("admin");
        if (!isAdmin && !id.toString().equals(call)) {
            logger.ErrorLog("GET /api/user/" + id + " , Id = " + call + " , forbidden");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"The user is not allowed to access this user\"}")
                    .build();
        }

        logger.Log("GET /api/user/" + id + " , Id = " + call);
        return Response.ok(new UserResponse(user)).build();
    }

    /**
     * PUT /api/user/{id} - Met à jour le nom affiché, le mot de passe et/ou l'avatar
     * d'un utilisateur. Un utilisateur non admin ne peut modifier que son propre profil.
     */
    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"user", "admin"})
    public Response updateUser(@PathParam("id") UUID id, UpdateUserRequest request) {
        UserModel user = userRepository.findById(id);
        String call = jwt.getSubject();
        if (user == null) {
            logger.ErrorLog("PUT /api/user/" + id + " , Id = " + call + " , user not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The user could not be found\"}")
                    .build();
        }

        boolean isAdmin = jwt.getGroups().contains("admin");
        if (!isAdmin && !id.toString().equals(call)) {
            logger.ErrorLog("PUT /api/user/" + id + " , Id = " + call + ", forbidden");
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
                logger.ErrorLog("PUT /api/user/" + id + " , Id = " + call + " , error password");
                return Response.status(401)
                        .entity("{\"message\": \"Error password\"}")
                        .build();
            }
        }
        
        if (request.avatar != null) {
            user.setAvatar(request.avatar);
        }

        logger.Log("PUT /api/user/" + id + " , Id = " + call + " , displayName = " + request.displayName + " , avatar = " + request.avatar);
        return Response.ok(new UserResponse(user)).build();
    }

    /**
     * DELETE /api/user/{id} - Supprime un utilisateur existant. Réservé aux admins et managers.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"admin", "manager"})
    public Response deleteUser(@PathParam("id") UUID id) {
        UserModel user = userRepository.findById(id);
        if (user == null) {
            logger.ErrorLog("DELETE /api/user/" + id + " , Id = " + jwt.getSubject() + " , user not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The user could not be found\"}")
                    .build();
        }

        userRepository.delete(user);

        logger.Log("DELETE /api/user/" + id + ", Id = " + jwt.getSubject());
        return Response.noContent().build();
    }

    

}
