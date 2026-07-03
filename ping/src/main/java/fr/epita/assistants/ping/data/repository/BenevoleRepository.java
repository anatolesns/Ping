package fr.epita.assistants.ping.data.repository;

import java.util.List;
import java.util.UUID;

import fr.epita.assistants.ping.data.model.BenevoleModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public class BenevoleRepository {

    @Inject
    EntityManager entityManager;

    public List<BenevoleModel> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BenevoleModel> cq = cb.createQuery(BenevoleModel.class);
        Root<BenevoleModel> root = cq.from(BenevoleModel.class);
        cq.select(root);
        return entityManager.createQuery(cq).getResultList();
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
