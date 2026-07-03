package fr.epita.assistants.ping.presentation.rest;

import fr.epita.assistants.ping.data.model.NotificationModel;
import fr.epita.assistants.ping.data.repository.NotificationRepository;
import fr.epita.assistants.ping.response.NotificationResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import fr.epita.assistants.ping.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/api/notification")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @Inject
    NotificationRepository notificationRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    Logger logger;

    /**
     * GET /api/notification/me - Retourne toutes les notifications de l'utilisateur
     * actuellement authentifié.
     */
    @GET
    @Path("/me")
    @RolesAllowed({"user", "admin", "manager"})
    public Response getMyNotifications() {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<NotificationModel> notifications = notificationRepository.findByUserId(userId);
        List<NotificationResponse> res = new ArrayList<>();
        for (NotificationModel n : notifications) {
            res.add(new NotificationResponse(n));
        }
        logger.Log("GET /api/notification/me, Id = " + jwt.getSubject());
        return Response.ok(res).build();
    }

    /**
     * GET /api/notification/me/unread-count - Retourne le nombre de notifications non lues
     * de l'utilisateur actuellement authentifié.
     */
    @GET
    @Path("/me/unread-count")
    @RolesAllowed({"user", "admin", "manager"})
    public Response getUnreadCount() {
        UUID userId = UUID.fromString(jwt.getSubject());
        long count = notificationRepository.countUnreadByUserId(userId);
        logger.Log("GET /api/notification/me/unread-count, Id = " + jwt.getSubject());
        return Response.ok("{\"count\": " + count + "}").build();
    }

    /**
     * PUT /api/notification/me/read-all - Marque toutes les notifications de l'utilisateur
     * actuellement authentifié comme lues.
     */
    @PUT
    @Path("/me/read-all")
    @Transactional
    @RolesAllowed({"user", "admin", "manager"})
    public Response markAllAsRead() {
        UUID userId = UUID.fromString(jwt.getSubject());
        notificationRepository.markAllAsReadForUser(userId);
        logger.Log("PUT /api/notification/me/read-all, Id = " + jwt.getSubject());
        return Response.noContent().build();
    }
}
