package park.bumsiku.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
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

    public List<Post> findAll(int page, int size) {
        return findAll(page, size, "ORDER BY p.createdAt DESC");
    }

    public List<Post> findAll(int page, int size, String orderByClause) {
        String jpql = "SELECT p FROM Post p " + orderByClause;
        TypedQuery<Post> query = entityManager.createQuery(jpql, Post.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }


    public int countAll() {
        String jpql = "SELECT COUNT(p) FROM Post p";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        return query.getSingleResult().intValue();
    }


    public List<Post> findAllByTagName(String tagName, int page, int size) {
        return findAllByTagName(tagName, page, size, "ORDER BY p.createdAt DESC");
    }

    public List<Post> findAllByTagName(String tagName, int page, int size, String orderByClause) {
        String jpql = "SELECT p FROM Post p WHERE p.id IN (SELECT DISTINCT pt.id FROM Post pt JOIN pt.tags t WHERE t.name = :tagName) " + orderByClause;
        TypedQuery<Post> query = entityManager.createQuery(jpql, Post.class);
        query.setParameter("tagName", tagName);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public int countByTagName(String tagName) {
        String jpql = "SELECT COUNT(DISTINCT p.id) FROM Post p JOIN p.tags t WHERE t.name = :tagName";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("tagName", tagName);
        return query.getSingleResult().intValue();
    }
}
