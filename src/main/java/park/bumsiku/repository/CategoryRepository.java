package park.bumsiku.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import park.bumsiku.domain.entity.Category;

import java.util.List;

@Deprecated(forRemoval = true)
@Repository
public class CategoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Deprecated(forRemoval = true)
    public List<Category> findAll() {
        return entityManager
                .createQuery("SELECT c FROM Category c ORDER BY c.ordernum ASC", Category.class)
                .getResultList();
    }

    @Deprecated(forRemoval = true)
    public Category findById(Integer id) {
        return entityManager.find(Category.class, id);
    }

    @Deprecated(forRemoval = true)
    public Category insert(Category category) {
        entityManager.persist(category);
        entityManager.flush();
        return category;
    }

    @Deprecated(forRemoval = true)
    public void delete(Integer id) {
        Category category = findById(id);
        if (category != null) {
            entityManager.remove(category);
        }
    }

    @Deprecated(forRemoval = true)
    public Category update(Category category) {
        Category existingCategory = findById(category.getId());
        if (existingCategory != null) {
            existingCategory.setName(category.getName());
            existingCategory.setOrdernum(category.getOrdernum());
            entityManager.merge(existingCategory);
            entityManager.flush();
            return existingCategory;
        }
        return null;
    }
}
