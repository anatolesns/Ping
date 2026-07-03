package fr.epita.assistants.ping.data.repository;

import java.util.List;
import java.util.UUID;

import fr.epita.assistants.ping.data.model.CreneauModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public class CreneauRepository {

    @Inject
    EntityManager entityManager;

    public List<CreneauModel> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CreneauModel> cq = cb.createQuery(CreneauModel.class);
        Root<CreneauModel> root = cq.from(CreneauModel.class);
        cq.select(root);
        return entityManager.createQuery(cq).getResultList();
    }

    public CreneauModel findById(UUID id) {
        return entityManager.find(CreneauModel.class, id);
    }

    public List<CreneauModel> findByJour(String jour) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CreneauModel> cq = cb.createQuery(CreneauModel.class);
        Root<CreneauModel> root = cq.from(CreneauModel.class);
        cq.select(root).where(cb.equal(root.get("jour"), jour));
        return entityManager.createQuery(cq).getResultList();
    }

    public List<CreneauModel> findByPoste(String poste) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CreneauModel> cq = cb.createQuery(CreneauModel.class);
        Root<CreneauModel> root = cq.from(CreneauModel.class);
        cq.select(root).where(cb.equal(root.get("poste"), poste));
        return entityManager.createQuery(cq).getResultList();
    }

    public List<CreneauModel> findByPrisParId(UUID userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CreneauModel> cq = cb.createQuery(CreneauModel.class);
        Root<CreneauModel> root = cq.from(CreneauModel.class);
        cq.select(root).where(cb.equal(root.get("prisPar").get("id"), userId));
        return entityManager.createQuery(cq).getResultList();
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
