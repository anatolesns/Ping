package fr.epita.assistants.ping.presentation.rest;

import fr.epita.assistants.ping.errors.ErrorsCode;
import fr.epita.assistants.ping.request.MoveRequest;
import fr.epita.assistants.ping.request.PathRequest;
import fr.epita.assistants.ping.response.FSEntryResponse;
import fr.epita.assistants.ping.service.FolderService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@jakarta.ws.rs.Path("/api/folders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class FolderResource {

    @Inject
    FolderService fs;

    @GET
    public Response listFolder(@QueryParam("path") String path) {
        Path target = fs.resolve(path);
        if (target == null) {
            throw ErrorsCode.PATH_TRAVERSAL.get(path);
        }
        if (!Files.exists(target) || !Files.isDirectory(target)) {
            throw ErrorsCode.NOT_FOUND_ERROR.get("folder " + path);
        }

        List<FSEntryResponse> entries = new ArrayList<>();
        Path root = fs.getRoot();
        try (Stream<Path> children = Files.list(target)) {
            for (Path child : (Iterable<Path>) children::iterator) {
                String name = child.getFileName().toString();
                String rel = root.relativize(child).toString();
                Boolean isDir = Files.isDirectory(child);
                entries.add(new FSEntryResponse(name, rel, isDir));
            }
        } catch (IOException e) {
            throw ErrorsCode.NOT_FOUND_ERROR.get("folder " + path);
        }
        return Response.ok(entries).build();
    }

    @POST
    public Response createFolder(PathRequest request) {
        if (request == null || request.relativePath == null || request.relativePath.isBlank()) {
            throw ErrorsCode.INVALID_PATH.get("null or empty");
        }
        Path target = fs.resolve(request.relativePath);
        if (target == null) {
            throw ErrorsCode.PATH_TRAVERSAL.get(request.relativePath);
        }
        if (Files.exists(target)) {
            throw ErrorsCode.ALREADY_EXISTS.get("folder " + request.relativePath);
        }
        try {
            Files.createDirectories(target);
        } catch (IOException e) {
            throw ErrorsCode.NOT_FOUND_ERROR.get("project");
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    public Response deleteFolder(PathRequest request) {
        if (request == null || request.relativePath == null) {
            throw ErrorsCode.INVALID_PATH.get("null");
        }
        Path target = fs.resolve(request.relativePath);
        if (target == null) {
            throw ErrorsCode.PATH_TRAVERSAL.get(request.relativePath);
        }
        if (!Files.exists(target) || !Files.isDirectory(target)) {
            throw ErrorsCode.NOT_FOUND_ERROR.get("folder " + request.relativePath);
        }

        Path root = fs.getRoot();
        try {
            if (target.equals(root)) {
                emptyDirectory(target);
            } else {
                deleteRecursively(target);
            }
        } catch (IOException e) {
            throw ErrorsCode.NOT_FOUND_ERROR.get("folder " + request.relativePath);
        }
        return Response.noContent().build();
    }

    @PUT
    @jakarta.ws.rs.Path("/move")
    public Response moveFolder(MoveRequest request) {
        if (request == null || request.src == null || request.src.isBlank()
                || request.dst == null || request.dst.isBlank()) {
            throw ErrorsCode.INVALID_PATH.get("source or destination null or empty");
        }
        Path src = fs.resolve(request.src);
        Path dst = fs.resolve(request.dst);
        if (src == null || dst == null) {
            throw ErrorsCode.PATH_TRAVERSAL.get(request.src + " -> " + request.dst);
        }
        if (!Files.exists(src) || !Files.isDirectory(src)) {
            throw ErrorsCode.NOT_FOUND_ERROR.get("source folder " + request.src);
        }
        if (Files.exists(dst)) {
            throw ErrorsCode.ALREADY_EXISTS.get("destination folder " + request.dst);
        }
        try {
            Files.move(src, dst);
        } catch (IOException e) {
            throw ErrorsCode.NOT_FOUND_ERROR.get("project");
        }
        return Response.noContent().build();
    }

    private void emptyDirectory(Path dir) throws IOException {
        try (Stream<Path> children = Files.list(dir)) {
            for (Path child : (Iterable<Path>) children::iterator) {
                deleteRecursively(child);
            }
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> children = Files.list(path)) {
                for (Path child : (Iterable<Path>) children::iterator) {
                    deleteRecursively(child);
                }
            }
        }
        Files.delete(path);
    }
}