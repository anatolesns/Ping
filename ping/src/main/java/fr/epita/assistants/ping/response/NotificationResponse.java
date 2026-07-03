package fr.epita.assistants.ping.response;

import fr.epita.assistants.ping.data.model.NotificationModel;

import java.time.Instant;
import java.util.UUID;

public class NotificationResponse {
    public UUID id;
    public String title;
    public String message;
    public String type;
    public Instant createdAt;
    public Boolean isRead;

    /**
     * Constructeur vide requis pour la (dé)sérialisation JSON.
     */
    public NotificationResponse() {}

    /**
     * Construit la réponse à partir d'une notification persistée, en
     * n'exposant que les champs destinés au client.
     */
    public NotificationResponse(NotificationModel notification) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.type = notification.getType();
        this.createdAt = notification.getCreatedAt();
        this.isRead = notification.getIsRead();
    }
}
