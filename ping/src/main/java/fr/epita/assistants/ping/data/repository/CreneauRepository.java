package fr.epita.assistants.ping.data.repository;

import java.util.List;
import java.util.UUID;

import fr.epita.assistants.ping.data.model.CreneauModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class CreneauRepository {

    @Inject
    EntityManager entityManager;

    public List<CreneauModel> findAll() {
        return entityManager
                .createQuery("SELECT c FROM CreneauModel c", CreneauModel.class)
                .getResultList();
    }

    public CreneauModel findById(UUID id) {
        return entityManager.find(CreneauModel.class, id);
    }

    public List<CreneauModel> findByJour(String jour) {
        return entityManager.createQuery(
                "SELECT c FROM CreneauModel c WHERE c.jour = :jour", CreneauModel.class)
                .setParameter("jour", jour)
                .getResultList();
    }

    public List<CreneauModel> findByPoste(String poste) {
        return entityManager.createQuery(
                "SELECT c FROM CreneauModel c WHERE c.poste = :poste", CreneauModel.class)
                .setParameter("poste", poste)
                .getResultList();
    }

    public List<CreneauModel> findByPrisParId(UUID userId) {
        return entityManager.createQuery(
                "SELECT c FROM CreneauModel c WHERE c.prisPar.id = :userId", CreneauModel.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public void save(CreneauModel creneau) {
        entityManager.persist(creneau);
    }

    public void delete(CreneauModel creneau) {
        CreneauModel managed;
        if (entityManager.contains(creneau)) {
            managed = creneau;
        } else {
            managed = entityManager.merge(creneau);
        }
        entityManager.remove(managed);
    }
}
