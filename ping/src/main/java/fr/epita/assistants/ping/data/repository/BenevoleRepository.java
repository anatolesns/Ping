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

    public List<BenevoleModel> findAll() {
        return entityManager
                .createQuery("SELECT b FROM BenevoleModel b", BenevoleModel.class)
                .getResultList();
    }

    public BenevoleModel findById(UUID id) {
        return entityManager.find(BenevoleModel.class, id);
    }

    public void save(BenevoleModel benevole) {
        entityManager.persist(benevole);
    }

    public BenevoleModel update(BenevoleModel benevole) {
        return entityManager.merge(benevole);
    }

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
