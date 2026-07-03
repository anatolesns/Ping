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

    /**
     * Récupère la liste de tous les créneaux.
     */
    public List<CreneauModel> findAll() {
        return entityManager
                .createQuery("SELECT c FROM CreneauModel c", CreneauModel.class)
                .getResultList();
    }

    /**
     * Recherche un créneau par son identifiant.
     */
    public CreneauModel findById(UUID id) {
        return entityManager.find(CreneauModel.class, id);
    }

    /**
     * Récupère les créneaux correspondant à un jour donné.
     */
    public List<CreneauModel> findByJour(String jour) {
        return entityManager.createQuery(
                "SELECT c FROM CreneauModel c WHERE c.jour = :jour", CreneauModel.class)
                .setParameter("jour", jour)
                .getResultList();
    }

    /**
     * Récupère les créneaux correspondant à un poste donné.
     */
    public List<CreneauModel> findByPoste(String poste) {
        return entityManager.createQuery(
                "SELECT c FROM CreneauModel c WHERE c.poste = :poste", CreneauModel.class)
                .setParameter("poste", poste)
                .getResultList();
    }

    /**
     * Récupère les créneaux pris par un utilisateur donné.
     */
    public List<CreneauModel> findByPrisParId(UUID userId) {
        return entityManager.createQuery(
                "SELECT c FROM CreneauModel c WHERE c.prisPar.id = :userId", CreneauModel.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Persiste un nouveau créneau en base.
     */
    public void save(CreneauModel creneau) {
        entityManager.persist(creneau);
    }

    /**
     * Supprime un créneau, en le rattachant au contexte de persistance si besoin.
     */
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
