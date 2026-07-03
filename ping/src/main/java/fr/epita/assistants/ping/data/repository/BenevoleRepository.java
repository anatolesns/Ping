package fr.epita.assistants.ping.data.repository;

import java.util.List;
import java.util.UUID;

import fr.epita.assistants.ping.data.model.BenevoleModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class BenevoleRepository {

    @Inject
    EntityManager entityManager;

    /**
     * Récupère la liste de tous les bénévoles enregistrés.
     */
    public List<BenevoleModel> findAll() {
        return entityManager
                .createQuery("SELECT b FROM BenevoleModel b", BenevoleModel.class)
                .getResultList();
    }

    /**
     * Recherche un bénévole par son identifiant.
     */
    public BenevoleModel findById(UUID id) {
        return entityManager.find(BenevoleModel.class, id);
    }

    /**
     * Persiste un nouveau bénévole en base.
     */
    public void save(BenevoleModel benevole) {
        entityManager.persist(benevole);
    }

    /**
     * Met à jour un bénévole existant en base.
     */
    public BenevoleModel update(BenevoleModel benevole) {
        return entityManager.merge(benevole);
    }

    /**
     * Supprime un bénévole, en le rattachant au contexte de persistance si besoin.
     */
    public void delete(BenevoleModel benevole) {
        BenevoleModel managed;
        if (entityManager.contains(benevole)) {
            managed = benevole;
        } else {
            managed = entityManager.merge(benevole);
        }
        entityManager.remove(managed);
    }
}
