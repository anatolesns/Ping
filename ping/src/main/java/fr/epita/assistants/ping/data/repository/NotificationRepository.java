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

    public List<NotificationModel> findByUserId(UUID userId) {
        return entityManager
                .createQuery("SELECT n FROM NotificationModel n WHERE n.user.id = :userId ORDER BY n.createdAt DESC", NotificationModel.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public long countUnreadByUserId(UUID userId) {
        return entityManager
                .createQuery("SELECT COUNT(n) FROM NotificationModel n WHERE n.user.id = :userId AND n.isRead = false", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public NotificationModel findById(UUID id) {
        return entityManager.find(NotificationModel.class, id);
    }

    public void save(NotificationModel notification) {
        entityManager.persist(notification);
    }

    public void markAllAsReadForUser(UUID userId) {
        entityManager
                .createQuery("UPDATE NotificationModel n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
