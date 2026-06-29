package fr.epita.assistants.ping.presentation.rest;

import fr.epita.assistants.ping.data.model.CreneauModel;
import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.ping.data.repository.CreneauRepository;
import fr.epita.assistants.ping.data.repository.UserRepository;
import fr.epita.assistants.ping.request.NewCreneauRequest;
import fr.epita.assistants.ping.response.CreneauResponse;
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

@Path("/api/creneau")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreneauResource {

    @Inject
    CreneauRepository creneauRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    Logger logger;

    @GET
    @Path("/all")
    @RolesAllowed({"user", "admin"})
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
    @RolesAllowed("admin")
    public Response createCreneau(NewCreneauRequest request) {
        if (request.jour == null || request.heureDebut == null || request.heureFin == null || request.poste == null) {
            logger.ErrorLog("POST /api/creneau, Id = " + jwt.getSubject() + ", missing required fields");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"All fields are required: jour, heureDebut, heureFin, poste\"}")
                    .build();
        }

        CreneauModel creneau = new CreneauModel(request.jour, request.heureDebut, request.heureFin, request.poste);
        creneauRepository.save(creneau);
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
        logger.Log("PUT /api/creneau/" + id + "/inscription, Id = " + jwt.getSubject());
        return Response.ok(new CreneauResponse(creneau)).build();
    }

    @PUT
    @Path("/{id}/desinscription")
    @Transactional
    @RolesAllowed({"user", "admin"})
    public Response desinscrire(@PathParam("id") UUID id) {
        CreneauModel creneau = creneauRepository.findById(id);
        if (creneau == null) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/desinscription, Id = " + jwt.getSubject() + ", creneau not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Creneau not found\"}")
                    .build();
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("admin");

        if (creneau.getPrisPar() == null || (!isAdmin && !creneau.getPrisPar().getId().equals(userId))) {
            logger.ErrorLog("PUT /api/creneau/" + id + "/desinscription, Id = " + jwt.getSubject() + ", forbidden");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Not allowed to unregister from this creneau\"}")
                    .build();
        }

        creneau.setIsPris(false);
        creneau.setPrisPar(null);
        logger.Log("PUT /api/creneau/" + id + "/desinscription, Id = " + jwt.getSubject());
        return Response.ok(new CreneauResponse(creneau)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("admin")
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
