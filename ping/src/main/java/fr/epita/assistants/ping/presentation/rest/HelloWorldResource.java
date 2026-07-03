package fr.epita.assistants.ping.presentation.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static fr.epita.assistants.ping.errors.ErrorsCode.EXAMPLE_ERROR;

@Path("/api")
public class HelloWorldResource {

    /**
     * GET /api/hello - Endpoint de test qui retourne un simple message "Hello World !".
     */
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public Response helloWorld() {
        return Response.ok("Hello World !").build();
    }

    /**
     * GET /api/error - Endpoint de démonstration qui déclenche volontairement une erreur
     * pour tester la gestion des exceptions.
     */
    @GET
    @Path("/error")
    @Produces(MediaType.APPLICATION_JSON)
    public Response error() {
        EXAMPLE_ERROR.throwException("This is an error");
        // This line will never be reached
        return Response.noContent().build();
    }
}