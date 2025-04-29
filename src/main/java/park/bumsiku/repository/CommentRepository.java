package park.bumsiku.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;

import java.util.List;

@Repository
public class CommentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Comment findById(Long id) {
        return entityManager.find(Comment.class, id);
    }

    public void delete(Long id) {
        Comment comment = findById(id);
        if (comment != null) {
            entityManager.remove(comment);
        }
    }

    public List<Comment> findAllByPost(Post post) {
        TypedQuery<Comment> query = entityManager.createQuery(
                "SELECT c FROM Comment c WHERE c.post = :post", Comment.class
        );
        query.setParameter("post", post);
        return query.getResultList();
    }

    public Comment insert(Comment comment) {
        entityManager.persist(comment);
        return comment;
    }
}