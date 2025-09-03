package park.bumsiku.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import park.bumsiku.domain.dto.response.PostSummaryResponse;
import park.bumsiku.domain.entity.Post;

import java.util.List;

@Repository
public class PostRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Post insert(Post post) {
        entityManager.persist(post);
        entityManager.flush();
        return post;
    }

    public Post update(Post post) {
        return entityManager.merge(post);
    }

    public void delete(Integer id) {
        Post post = findById(id);
        if (post != null) {
            entityManager.remove(post);
        }
    }

    public Post findById(Integer id) {
        return entityManager.find(Post.class, id);
    }

    public List<PostSummaryResponse> findAll(int page, int size) {
        return findAll(page, size, "ORDER BY p.createdAt DESC");
    }

    public List<PostSummaryResponse> findAll(int page, int size, String orderByClause) {
        String jpql = buildPostSummarySelectClause() +
                "FROM Post p " + orderByClause;
        TypedQuery<PostSummaryResponse> query =
                entityManager.createQuery(jpql, PostSummaryResponse.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public List<PostSummaryResponse> findAllByCategoryId(int categoryId, int page, int size) {
        return findAllByCategoryId(categoryId, page, size, "ORDER BY p.createdAt DESC");
    }

    public List<PostSummaryResponse> findAllByCategoryId(int categoryId, int page, int size, String orderByClause) {
        String jpql = buildPostSummarySelectClause() +
                "FROM Post p WHERE p.category.id = :categoryId " + orderByClause;
        TypedQuery<PostSummaryResponse> query =
                entityManager.createQuery(jpql, PostSummaryResponse.class);
        query.setParameter("categoryId", categoryId);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public int countAll() {
        String jpql = "SELECT COUNT(p) FROM Post p";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        return query.getSingleResult().intValue();
    }

    public int countByCategoryId(int categoryId) {
        String jpql = "SELECT COUNT(p) FROM Post p WHERE p.category.id = :categoryId";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("categoryId", categoryId);
        return query.getSingleResult().intValue();
    }

    private String buildPostSummarySelectClause() {
        return "SELECT new park.bumsiku.domain.dto.response.PostSummaryResponse(" +
                "p.id, p.title, p.summary, p.category.id, p.createdAt, p.updatedAt, p.views) ";
    }
}
