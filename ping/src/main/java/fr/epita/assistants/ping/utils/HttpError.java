package fr.epita.assistants.ping.utils;

import java.util.function.Supplier;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import static jakarta.ws.rs.core.Response.status;

public class HttpError implements Supplier<RuntimeException>, IHttpError {
    private final Status status;
    private final String message;

    /**
     * Construit une erreur HTTP réutilisable, associée à un statut et un
     * message (potentiellement paramétrable).
     */
    public HttpError(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Construit l'exception JAX-RS correspondant au statut et au message
     * formaté avec les arguments fournis.
     */
    private RuntimeException createError(Object... args) {
        return new WebApplicationException(
                status(status)
                        .entity(new ErrorInfo(String.format(message, args)))
                        .build());
    }

    /**
     * Fournit l'exception correspondant à cette erreur, sans arguments de formatage.
     */
    @Override
    public RuntimeException get() {
        return createError();
    }

    /**
     * Fournit l'exception correspondant à cette erreur, avec des arguments
     * de formatage pour le message.
     */
    @Override
    public RuntimeException get(Object... args) {
        return createError(args);
    }

    /**
     * Lève directement l'exception correspondant à cette erreur.
     */
    @Override
    public void throwException(Object... args) {
        throw get(args);
    }
}
