package fr.epita.assistants.ping.data.repository;

import java.util.List;
import java.util.UUID;

import fr.epita.assistants.ping.data.model.NotificationModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public class NotificationRepository {

    @Inject
    EntityManager entityManager;

    public List<NotificationModel> findByUserId(UUID userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NotificationModel> cq = cb.createQuery(NotificationModel.class);
        Root<NotificationModel> root = cq.from(NotificationModel.class);
        cq.select(root)
                .where(cb.equal(root.get("user").get("id"), userId))
                .orderBy(cb.desc(root.get("createdAt")));
        return entityManager.createQuery(cq).getResultList();
    }

    public long countUnreadByUserId(UUID userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<NotificationModel> root = cq.from(NotificationModel.class);
        cq.select(cb.count(root))
                .where(cb.equal(root.get("user").get("id"), userId), cb.isFalse(root.get("isRead")));
        return entityManager.createQuery(cq).getSingleResult();
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
