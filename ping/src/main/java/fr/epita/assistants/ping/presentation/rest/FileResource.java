package fr.epita.assistants.ping.presentation.rest;
import fr.epita.assistants.ping.request.MoveRequest;
import fr.epita.assistants.ping.request.PathRequest;
import fr.epita.assistants.ping.service.FileService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.io.IOException;
import java.nio.file.Files;
import fr.epita.assistants.ping.utils.Logger;

@Path("/api/files")
public class FileResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    Logger logger;

    @Inject
    FileService fileService;

    /**
     * GET /api/files - Télécharge le contenu binaire d'un fichier situé au chemin relatif donné.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({"user", "admin"})
    public Response getFile(@QueryParam("path") String path) {
        if (path == null || path.isEmpty()) {
            logger.ErrorLog("GET /api/files, Id = " + jwt.getSubject() + " , path null");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        java.nio.file.Path res = fileService.resolve(path);
        if (res == null) {
            logger.ErrorLog("GET /api/files, Id = " + jwt.getSubject() + " , not allowed");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"The user is not allowed to access the project or a path traversal attack was detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (!Files.exists(res) || Files.isDirectory(res)) {
            logger.ErrorLog("GET /api/files, Id = " + jwt.getSubject() + " , Files not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"File not found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            byte[] content = Files.readAllBytes(res);
            logger.Log("GET /api/files, Id = " + jwt.getSubject() + " , path = " + path);
            return Response.ok(content, MediaType.APPLICATION_OCTET_STREAM).build();
        } catch (IOException e) {
            logger.ErrorLog("GET /api/files, Id = " + jwt.getSubject() + " , error get");
            return Response.status(500)
                    .entity("{\"message\": \"Error file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    /**
     * POST /api/files - Crée un fichier vide au chemin relatif donné, s'il n'existe pas déjà.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response createFile(PathRequest request) {
        if (request == null) {
            logger.ErrorLog("GET /api/files, Id = " + jwt.getSubject() + " , invalid path");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (request.relativePath == null) {
            logger.ErrorLog("POST /api/files, Id = " + jwt.getSubject() + " , error path");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        java.nio.file.Path res = fileService.resolve(request.relativePath);
        if (res == null) {
            logger.ErrorLog("POST /api/files, Id = " + jwt.getSubject() + " , not allowed");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"The user is not allowed to access the project or a path traversal attack was detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (Files.exists(res)) {
            logger.ErrorLog("POST /api/files, Id = " + jwt.getSubject() + " , already exists");
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"File already exists\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        java.nio.file.Path parent = res.getParent();
        if (!Files.exists(parent)) {
            logger.ErrorLog("POST /api/files, Id = " + jwt.getSubject() + " , not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The project or the file could not be found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            Files.createFile(res);
            logger.Log("POST /api/files, Id = " + jwt.getSubject() + " , path = " + request.relativePath);
            return Response.status(Response.Status.CREATED).build();
        } catch (IOException e) {
            logger.ErrorLog("POST /api/files, Id = " + jwt.getSubject() + " , error post");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    /**
     * DELETE /api/files - Supprime le fichier situé au chemin relatif donné.
     */
    @DELETE
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response deleteFile(PathRequest request) {
        if (request == null) {
            logger.ErrorLog("DELETE /api/files, Id = " + jwt.getSubject() + " , request null");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (request.relativePath == null || request.relativePath.isEmpty()) {
            logger.ErrorLog("DELETE /api/files, Id = " + jwt.getSubject() + " , path null or empty");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        java.nio.file.Path res = fileService.resolve(request.relativePath);
        if (res == null) {
            logger.ErrorLog("DELETE /api/files, Id = " + jwt.getSubject() + " , path = " + request.relativePath + " , not allowed");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Path attack detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (!Files.exists(res) || Files.isDirectory(res)) {
            logger.ErrorLog("DELETE /api/files, Id = " + jwt.getSubject() + " , path = " + request.relativePath + " , file not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"File not found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            Files.delete(res);
            logger.Log("DELETE /api/files, Id = " + jwt.getSubject() + " , path = " + request.relativePath);
            return Response.noContent().build();
        } catch (IOException e) {
            logger.ErrorLog("DELETE /api/files, Id = " + jwt.getSubject() + " , path = " + request.relativePath + " , error delete");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    /**
     * PUT /api/files/move - Déplace/renomme un fichier depuis le chemin source vers le
     * chemin destination fournis.
     */
    @PUT
    @Path("/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response moveFile(MoveRequest request) {
        if (request == null || request.src == null || request.src.isEmpty()
                || request.dst == null || request.dst.isEmpty()) {
            logger.ErrorLog("PUT /api/files/move, Id = " + jwt.getSubject() + " , invalid req");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The source or destination path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        java.nio.file.Path source = fileService.resolve(request.src);
        java.nio.file.Path dest = fileService.resolve(request.dst);

        if (source == null || dest == null) {
            logger.ErrorLog("PUT /api/files/move, Id = " + jwt.getSubject() + " , src = " + request.src + " , dst = " + request.dst + " , not allowed");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Path detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (!Files.exists(source)) {
            logger.ErrorLog("PUT /api/files/move, Id = " + jwt.getSubject() + " , src = " + request.src + " , not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The project could not be found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (Files.exists(dest)) {
            logger.ErrorLog("PUT /api/files/move, Id = " + jwt.getSubject() + " , dst = " + request.dst + " , already exists");
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"The destination file already exists\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            Files.move(source, dest);
            logger.Log("PUT /api/files/move, Id = " + jwt.getSubject() + " , src = " + request.src + " , dst = " + request.dst);
            return Response.noContent().build();
        } catch (IOException e) {
            logger.ErrorLog("PUT /api/files/move, Id = " + jwt.getSubject() + " , src = " + request.src + " , dst = " + request.dst + " , error move");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error moving file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    /**
     * POST /api/files/upload - Écrit le contenu binaire reçu dans le fichier situé au
     * chemin relatif donné (création ou remplacement).
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({"user", "admin"})
    public Response uploadFile(@QueryParam("path") String path, byte[] content) {
        if (path == null || path.isEmpty()) {
            logger.ErrorLog("POST /api/files/upload, Id = " + jwt.getSubject() + " , path null");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        java.nio.file.Path res = fileService.resolve(path);
        if (res == null) {
            logger.ErrorLog("POST /api/files/upload, Id = " + jwt.getSubject() + " , path = " + path + " , not allowed");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Path traversal attack detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        java.nio.file.Path parent = res.getParent();
        if (!Files.exists(parent)) {
            logger.ErrorLog("POST /api/files/upload, Id = " + jwt.getSubject() + " , path = " + path + " , not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The project could not be found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            Files.write(res, content);
            logger.Log("POST /api/files/upload, Id = " + jwt.getSubject() + " , path = " + path);
            return Response.status(Response.Status.CREATED).build();
        } catch (IOException e) {
            logger.ErrorLog("POST /api/files/upload, Id = " + jwt.getSubject() + " , path = " + path + " , error upload");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
