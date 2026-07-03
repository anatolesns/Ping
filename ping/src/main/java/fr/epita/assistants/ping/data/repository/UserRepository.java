package fr.epita.assistants.ping.data.repository;

import java.util.List;
import java.util.UUID;

import fr.epita.assistants.ping.data.model.UserModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public class UserRepository {

    @Inject
    EntityManager entityManager;

    public List<UserModel> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserModel> cq = cb.createQuery(UserModel.class);
        Root<UserModel> root = cq.from(UserModel.class);
        cq.select(root);
        return entityManager.createQuery(cq).getResultList();
    }

    public UserModel findById(UUID id) {
        return entityManager.find(UserModel.class, id);
    }

    public UserModel findByLogin(String login) throws NoResultException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserModel> cq = cb.createQuery(UserModel.class);
        Root<UserModel> root = cq.from(UserModel.class);
        cq.select(root).where(cb.equal(root.get("login"), login));
        return entityManager.createQuery(cq).getSingleResult();
    }

    public boolean loginExists(String login) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<UserModel> root = cq.from(UserModel.class);
        cq.select(cb.count(root)).where(cb.equal(root.get("login"), login));
        long count = entityManager.createQuery(cq).getSingleResult();
        return count != 0;
    }

    public void save(UserModel user) {
        entityManager.persist(user);
    }

    public void delete(UserModel user) {
    UserModel managed;
    
    if (entityManager.contains(user)) {
        managed = user;
    } else {
        managed = entityManager.merge(user);
    }
    
    entityManager.remove(managed);
}
}
