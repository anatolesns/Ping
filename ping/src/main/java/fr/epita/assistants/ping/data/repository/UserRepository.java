package fr.epita.assistants.ping.data.repository;

import java.util.List;
import java.util.UUID;

import fr.epita.assistants.ping.data.model.UserModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

@ApplicationScoped
public class UserRepository {

    @Inject
    EntityManager entityManager;

    public List<UserModel> findAll() {
        return entityManager
                .createQuery("SELECT u FROM UserModel u", UserModel.class)
                .getResultList();
    }

    public UserModel findById(UUID id) {
        return entityManager.find(UserModel.class, id);
    }

    public UserModel findByLogin(String login) throws NoResultException {
        return entityManager.createQuery(
                "SELECT u FROM UserModel u WHERE u.login = :login", UserModel.class)
                .setParameter("login", login)
                .getSingleResult();
    }

    public boolean loginExists(String login) {
        long count = entityManager.createQuery(
                "SELECT COUNT(u) FROM UserModel u WHERE u.login = :login", Long.class)
                .setParameter("login", login)
                .getSingleResult();
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
