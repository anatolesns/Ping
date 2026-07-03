package fr.epita.assistants.ping.presentation.rest;

import fr.epita.assistants.ping.data.model.BenevoleModel;
import fr.epita.assistants.ping.data.model.CreneauModel;
import fr.epita.assistants.ping.data.model.NotificationModel;
import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.ping.data.repository.BenevoleRepository;
import fr.epita.assistants.ping.data.repository.CreneauRepository;
import fr.epita.assistants.ping.data.repository.NotificationRepository;
import fr.epita.assistants.ping.data.repository.UserRepository;
import fr.epita.assistants.ping.request.NewCreneauRequest;
import fr.epita.assistants.ping.response.CreneauResponse;
import fr.epita.assistants.ping.service.CreneauService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import fr.epita.assistants.ping.utils.EmailUtils;
import fr.epita.assistants.ping.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/api/creneau")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreneauResource {

    @Inject
    CreneauRepository creneauRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    BenevoleRepository benevoleRepository;

    @Inject
    NotificationRepository notificationRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    Logger logger;

    @Inject
    CreneauService creneauService;

    @GET
    @Path("/all")
    @RolesAllowed({"user", "admin", "manager"})
    public Response getAllCreneaux() {
        List<CreneauModel> all = creneauRepository.findAll();
        List<CreneauResponse> res = new ArrayList<>();
        for (CreneauModel c : all) {
            res.add(new CreneauResponse(c));
        }
        logger.Log("GET /api/creneau/all, Id = " + jwt.getSubject());
        return Response.ok(res).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Response getCreneau(@PathParam("id") UUID id) {
        CreneauModel creneau = creneauRepository.findById(id);
        if (creneau == null) {
            logger.ErrorLog("GET /api/creneau/" + id + ", Id = " + jwt.getSubject() + ", creneau not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Creneau not found\"}")
                    .build();
        }
        logger.Log("GET /api/creneau/" + id + ", Id = " + jwt.getSubject());
        return Response.ok(new CreneauResponse(creneau)).build();
    }

    @GET
    @Path("/jour/{jour}")
    @RolesAllowed({"user", "admin"})
    public Response getCreneauxByJour(@PathParam("jour") String jour) {
        List<CreneauModel> creneaux = creneauRepository.findByJour(jour);
        List<CreneauResponse> res = new ArrayList<>();
        for (CreneauModel c : creneaux) {
            res.add(new CreneauResponse(c));
        }
        logger.Log("GET /api/creneau/jour/" + jour + ", Id = " + jwt.getSubject());
        return Response.ok(res).build();
    }

    @GET
    @Path("/poste/{poste}")
    @RolesAllowed({"user", "admin"})
    public Response getCreneauxByPoste(@PathParam("poste") String poste) {
        List<CreneauModel> creneaux = creneauRepository.findByPoste(poste);
        List<CreneauResponse> res = new ArrayList<>();
        for (CreneauModel c : creneaux) {
            res.add(new CreneauResponse(c));
        }
        logger.Log("GET /api/creneau/poste/" + poste + ", Id = " + jwt.getSubject());
        return Response.ok(res).build();
    }

    @GET
    @Path("/mes-creneaux")
    @RolesAllowed({"user", "admin"})
    public Response getMesCreneaux() {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<CreneauModel> creneaux = creneauRepository.findByPrisParId(userId);
        List<CreneauResponse> res = new ArrayList<>();
        for (CreneauModel c : creneaux) {
            res.add(new CreneauResponse(c));
        }
        logger.Log("GET /api/creneau/mes-creneaux, Id = " + jwt.getSubject());
        return Response.ok(res).build();
    }

    @POST
    @Transactional
    @RolesAllowed({"admin", "manager"})
    public Response createCreneau(NewCreneauRequest request) {
        if (request.jour == null || request.heureDebut == null || request.heureFin == null || request.poste == null) {
            logger.ErrorLog("POST /api/creneau, Id = " + jwt.getSubject() + ", missing required fields");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"All fields are required: jour, heureDebut, heureFin, poste\"}")
                    .build();
        }

        CreneauModel creneau = new CreneauModel(request.jour, request.heureDebut, request.heureFin, request.poste);
        creneauRepository.save(creneau);

        String message = "Un nouveau créneau à la " + creneauService.posteLabel(creneau.getPoste()) + " est disponible le "
                + creneau.getJour() + " " + creneau.getHeureDebut() + "-" + creneau.getHeureFin();
        for (BenevoleModel benevole : benevoleRepository.findAll()) {
            notificationRepository.save(new NotificationModel(
                    benevole.getUser(), "Nouveau créneau disponible", message, "NOUVEAU_CRENEAU"));
        }

        logger.Log("POST /api/creneau, Id = " + jwt.getSubject() + ", jour = " + request.jour + ", poste = " + request.poste);
        return Response.ok(new CreneauResponse(creneau)).build();
    }

    @PUT
    @Path("/{id}/inscription")
    @Transactional
    @RolesAllowed({"user", "admin"})
    public Response inscrire(@PathParam("id") UUID id) {
        CreneauModel creneau = creneauRepository.findById(id);
        if (creneau == null) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/inscription, Id = " + jwt.getSubject() + ", creneau not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Creneau not found\"}")
                    .build();
        }

        if (creneau.getIsPris() != null && creneau.getIsPris()) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/inscription, Id = " + jwt.getSubject() + ", creneau deja pris");
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Ce creneau est deja pris\"}")
                    .build();
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        UserModel user = userRepository.findById(userId);
        if (user == null) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/inscription, Id = " + jwt.getSubject() + ", user not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"User not found\"}")
                    .build();
        }

        creneau.setIsPris(true);
        creneau.setPrisPar(user);

        String message = "Vous êtes bien inscrit au créneau " + creneau.getJour() + " "
                + creneau.getHeureDebut() + "-" + creneau.getHeureFin() + " - " + creneauService.posteLabel(creneau.getPoste());
        notificationRepository.save(new NotificationModel(
                user, "Confirmation d'inscription", message, "CONFIRMATION"));

        BenevoleModel benevole = benevoleRepository.findById(userId);
        if (benevole != null && benevole.getEmail() != null && !benevole.getEmail().isBlank()) {
            try {
                EmailUtils.sendAssignment(benevole.getEmail(), benevole.getNomComplet(),
                        creneau.getJour(), creneau.getHeureDebut(), creneau.getHeureFin(),
                        creneauService.posteLabel(creneau.getPoste()), creneauService.posteWhatsapp(creneau.getPoste()));
                logger.Log("PUT /api/creneau/" + id + "/inscription, assignment email sent to " + benevole.getEmail());
            } catch (Exception e) {
                logger.ErrorLog("PUT /api/creneau/" + id + "/inscription, email failed: " + e.getMessage());
            }
        }

        logger.Log("PUT /api/creneau/" + id + "/inscription, Id = " + jwt.getSubject());
        return Response.ok(new CreneauResponse(creneau)).build();
    }

    @PUT
    @Path("/{id}/assign/{benevoleId}")
    @Transactional
    @RolesAllowed({"admin", "manager"})
    public Response assigner(@PathParam("id") UUID id, @PathParam("benevoleId") UUID benevoleId) {
        CreneauModel creneau = creneauRepository.findById(id);
        if (creneau == null) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/assign/" + benevoleId + ", Id = " + jwt.getSubject() + ", creneau not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Creneau not found\"}")
                    .build();
        }

        if (creneau.getIsPris() != null && creneau.getIsPris()) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/assign/" + benevoleId + ", Id = " + jwt.getSubject() + ", creneau deja pris");
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Ce creneau est deja pris\"}")
                    .build();
        }

        UserModel user = userRepository.findById(benevoleId);
        if (user == null) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/assign/" + benevoleId + ", Id = " + jwt.getSubject() + ", benevole not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Benevole not found\"}")
                    .build();
        }

        creneau.setIsPris(true);
        creneau.setPrisPar(user);

        String message = "Vous avez été assigné au créneau " + creneau.getJour() + " "
                + creneau.getHeureDebut() + "-" + creneau.getHeureFin() + " - " + creneauService.posteLabel(creneau.getPoste());
        notificationRepository.save(new NotificationModel(
                user, "Affectation à un créneau", message, "CONFIRMATION"));

        BenevoleModel benevoleAssigne = benevoleRepository.findById(benevoleId);
        if (benevoleAssigne != null && benevoleAssigne.getEmail() != null && !benevoleAssigne.getEmail().isBlank()) {
            try {
                EmailUtils.sendAssignment(benevoleAssigne.getEmail(), benevoleAssigne.getNomComplet(),
                        creneau.getJour(), creneau.getHeureDebut(), creneau.getHeureFin(),
                        creneauService.posteLabel(creneau.getPoste()), creneauService.posteWhatsapp(creneau.getPoste()));
                logger.Log("PUT /api/creneau/" + id + "/assign/" + benevoleId + ", assignment email sent to " + benevoleAssigne.getEmail());
            } catch (Exception e) {
                logger.ErrorLog("PUT /api/creneau/" + id + "/assign/" + benevoleId + ", email failed: " + e.getMessage());
            }
        }

        logger.Log("PUT /api/creneau/" + id + "/assign/" + benevoleId + ", Id = " + jwt.getSubject());
        return Response.ok(new CreneauResponse(creneau)).build();
    }

    @PUT
    @Path("/{id}/desinscription")
    @Transactional
    @RolesAllowed({"user", "admin", "manager"})
    public Response desinscrire(@PathParam("id") UUID id) {
        CreneauModel creneau = creneauRepository.findById(id);
        if (creneau == null) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/desinscription, Id = " + jwt.getSubject() + ", creneau not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Creneau not found\"}")
                    .build();
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("admin") || jwt.getGroups().contains("manager");

        if (creneau.getPrisPar() == null || (!isAdmin && !creneau.getPrisPar().getId().equals(userId))) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/desinscription, Id = " + jwt.getSubject() + ", forbidden");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Not allowed to unregister from this creneau\"}")
                    .build();
        }

        UserModel quitte = creneau.getPrisPar();
        BenevoleModel benevoleQuitte = benevoleRepository.findById(quitte.getId());
        String nomQuitte = benevoleQuitte != null && benevoleQuitte.getNomComplet() != null
                ? benevoleQuitte.getNomComplet() : quitte.getLogin();

        creneau.setIsPris(false);
        creneau.setPrisPar(null);

        String message = nomQuitte + " a quitté le créneau " + creneau.getJour() + " "
                + creneau.getHeureDebut() + "-" + creneau.getHeureFin() + " - " + creneauService.posteLabel(creneau.getPoste());
        for (UserModel admin : userRepository.findAll()) {
            if (Boolean.TRUE.equals(admin.getIsAdmin())) {
                notificationRepository.save(new NotificationModel(
                        admin, "Désistement d'un créneau", message, "DESISTEMENT"));
            }
        }

        logger.Log("PUT /api/creneau/" + id + "/desinscription, Id = " + jwt.getSubject());
        return Response.ok(new CreneauResponse(creneau)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"admin", "manager"})
    public Response deleteCreneau(@PathParam("id") UUID id) {
        CreneauModel creneau = creneauRepository.findById(id);
        if (creneau == null) {
            logger.ErrorLog("DELETE /api/creneau/" + id + ", Id = " + jwt.getSubject() + ", creneau not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Creneau not found\"}")
                    .build();
        }

        creneauRepository.delete(creneau);
        logger.Log("DELETE /api/creneau/" + id + ", Id = " + jwt.getSubject());
        return Response.noContent().build();
    }
}
