package fr.epita.assistants.ping.data.repository;

import java.util.List;
import java.util.UUID;

import fr.epita.assistants.ping.data.model.NotificationModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class NotificationRepository {

    @Inject
    EntityManager entityManager;

    /**
     * Récupère les notifications d'un utilisateur, triées de la plus récente à la plus ancienne.
     */
    public List<NotificationModel> findByUserId(UUID userId) {
        return entityManager
                .createQuery("SELECT n FROM NotificationModel n WHERE n.user.id = :userId ORDER BY n.createdAt DESC", NotificationModel.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Compte le nombre de notifications non lues pour un utilisateur.
     */
    public long countUnreadByUserId(UUID userId) {
        return entityManager
                .createQuery("SELECT COUNT(n) FROM NotificationModel n WHERE n.user.id = :userId AND n.isRead = false", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    /**
     * Recherche une notification par son identifiant.
     */
    public NotificationModel findById(UUID id) {
        return entityManager.find(NotificationModel.class, id);
    }

    /**
     * Persiste une nouvelle notification en base.
     */
    public void save(NotificationModel notification) {
        entityManager.persist(notification);
    }

    /**
     * Marque comme lues toutes les notifications non lues d'un utilisateur.
     */
    public void markAllAsReadForUser(UUID userId) {
        entityManager
                .createQuery("UPDATE NotificationModel n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
