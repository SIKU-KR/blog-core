package park.bumsiku.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import park.bumsiku.domain.entity.Tag;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    Optional<Tag> findByName(String name);

    Optional<Tag> findByNameIgnoreCase(String name);

    List<Tag> findByNameIn(List<String> names);

    List<Tag> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}