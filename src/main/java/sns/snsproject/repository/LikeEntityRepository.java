package sns.snsproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sns.snsproject.model.entity.LikeEntity;
import sns.snsproject.model.entity.PostEntity;
import sns.snsproject.model.entity.UserEntity;

import java.util.Optional;

@Repository
public interface LikeEntityRepository extends JpaRepository<LikeEntity, Long> {

    Optional<LikeEntity> findByUserAndPost(UserEntity userEntity, PostEntity postEntity);

    @Query(value = "select count(*) from LikeEntity entity where entity.post = :post")
    long countByPost(@Param("post") PostEntity postEntity);
}
