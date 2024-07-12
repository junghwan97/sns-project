package sns.snsproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.snsproject.model.entity.PostEntity;

@Repository
public interface PostEntityRepository extends JpaRepository<PostEntity, Long> {
}
