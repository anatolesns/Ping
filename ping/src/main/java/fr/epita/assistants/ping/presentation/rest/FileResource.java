package fr.epita.assistants.ping.presentation.rest;
import fr.epita.assistants.ping.request.MoveRequest;
import fr.epita.assistants.ping.request.PathRequest;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.io.IOException;
import java.nio.file.Files;

@Path("/api/files")
public class FileResource {

    @Inject
    JsonWebToken jwt;

    @ConfigProperty(name = "filesystem.default.path")
    String default_value;

    private java.nio.file.Path resolvePath(String path) {
        java.nio.file.Path root = java.nio.file.Path.of(default_value).toAbsolutePath().normalize();
        return root.resolve(path).normalize();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({"user", "admin"})
    public Response getFile(@QueryParam("path") String path) {
        if (path == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        java.nio.file.Path root = java.nio.file.Path.of(default_value).toAbsolutePath().normalize();
        java.nio.file.Path temp = root.resolve(path).normalize();
        if (!temp.startsWith(root)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"The user is not allowed to access the project or a path traversal attack was detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        java.nio.file.Path res = resolvePath(path);

        if (!Files.exists(res) || Files.isDirectory(res)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"File not found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            byte[] content = Files.readAllBytes(res);
            return Response.ok(content, MediaType.APPLICATION_OCTET_STREAM).build();
        } catch (IOException e) {
            return Response.status(500)
                    .entity("{\"message\": \"Error file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response createFile(PathRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (request.relativePath == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        java.nio.file.Path root = java.nio.file.Path.of(default_value).toAbsolutePath().normalize();
        java.nio.file.Path temp = root.resolve(request.relativePath).normalize();
        if (!temp.startsWith(root)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"The user is not allowed to access the project or a path traversal attack was detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        java.nio.file.Path res = resolvePath(request.relativePath);

        if (Files.exists(res)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"File already exists\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        java.nio.file.Path parent = res.getParent();
        if (!Files.exists(parent)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The project or the file could not be found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            Files.createFile(res);
            return Response.status(Response.Status.CREATED).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @DELETE
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response deleteFile(PathRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (request.relativePath == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        java.nio.file.Path aux = java.nio.file.Path.of(default_value).toAbsolutePath().normalize();
        java.nio.file.Path temp = aux.resolve(request.relativePath).normalize();
        if (!temp.startsWith(aux)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Path attack detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        java.nio.file.Path res = resolvePath(request.relativePath);

        if (!Files.exists(res) || Files.isDirectory(res)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"File not found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            Files.delete(res);
            return Response.noContent().build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @PUT
    @Path("/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response moveFile(MoveRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The source or destination path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        java.nio.file.Path root = java.nio.file.Path.of(default_value).toAbsolutePath().normalize();
        java.nio.file.Path source = root.resolve(request.src).normalize();
        java.nio.file.Path dest = root.resolve(request.dst).normalize();

        if (!source.startsWith(root) || !dest.startsWith(root)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Path detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (!Files.exists(source)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The project could not be found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (Files.exists(dest)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"The destination file already exists\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            Files.move(source, dest);
            return Response.noContent().build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error moving file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({"user", "admin"})
    public Response uploadFile(@QueryParam("path") String path, byte[] content) {
        if (path == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"The relative path is invalid\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        java.nio.file.Path root = java.nio.file.Path.of(default_value).toAbsolutePath().normalize();
        java.nio.file.Path res = root.resolve(path).normalize();

        if (!res.startsWith(root)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Path traversal attack detected\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        java.nio.file.Path parent = res.getParent();
        if (!Files.exists(parent)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"The project could not be found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            Files.write(res, content);
            return Response.status(Response.Status.CREATED).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
