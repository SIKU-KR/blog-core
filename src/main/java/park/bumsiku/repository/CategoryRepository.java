package park.bumsiku.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import park.bumsiku.domain.entity.Category;

import java.util.List;

@Repository
public class CategoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Category> findAll() {
        return entityManager
                .createQuery("SELECT c FROM Category c ORDER BY c.orderNum ASC", Category.class)
                .getResultList();
    }

    public Category findById(Integer id) {
        return entityManager.find(Category.class, id);
    }

    public Category insert(Category category) {
        entityManager.persist(category);
        return category;
    }

    public void delete(Integer id) {
        Category category = findById(id);
        if (category != null) {
            entityManager.remove(category);
        }
    }

    public int update(Category category) {
        Query query = entityManager.createQuery(
                "UPDATE Category c SET c.name = :newName, c.orderNum = :newOrderNum WHERE c.id = :id"
        );
        query.setParameter("newName", category.getName());
        query.setParameter("newOrderNum", category.getOrdernum());
        query.setParameter("id", category.getId());
        return query.executeUpdate();
    }
}