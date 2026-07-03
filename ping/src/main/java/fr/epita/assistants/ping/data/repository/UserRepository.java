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

    /**
     * Récupère la liste de tous les utilisateurs.
     */
    public List<UserModel> findAll() {
        return entityManager
                .createQuery("SELECT u FROM UserModel u", UserModel.class)
                .getResultList();
    }

    /**
     * Recherche un utilisateur par son identifiant.
     */
    public UserModel findById(UUID id) {
        return entityManager.find(UserModel.class, id);
    }

    /**
     * Recherche un utilisateur par son login, lève une exception si aucun résultat.
     */
    public UserModel findByLogin(String login) throws NoResultException {
        return entityManager.createQuery(
                "SELECT u FROM UserModel u WHERE u.login = :login", UserModel.class)
                .setParameter("login", login)
                .getSingleResult();
    }

    /**
     * Vérifie si un login est déjà utilisé par un utilisateur existant.
     */
    public boolean loginExists(String login) {
        long count = entityManager.createQuery(
                "SELECT COUNT(u) FROM UserModel u WHERE u.login = :login", Long.class)
                .setParameter("login", login)
                .getSingleResult();
        return count != 0;
    }

    /**
     * Persiste un nouvel utilisateur en base.
     */
    public void save(UserModel user) {
        entityManager.persist(user);
    }

    /**
     * Supprime un utilisateur, en le rattachant au contexte de persistance si besoin.
     */
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
