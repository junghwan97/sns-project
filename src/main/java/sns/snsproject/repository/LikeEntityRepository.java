package sns.snsproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sns.snsproject.model.entity.LikeEntity;
import sns.snsproject.model.entity.PostEntity;
import sns.snsproject.model.entity.UserEntity;

import java.util.Optional;

@Repository
public interface LikeEntityRepository extends JpaRepository<LikeEntity, Long> {

    Optional<LikeEntity> findByUserAndPost(UserEntity userEntity, PostEntity postEntity);

    @Query(value = "select count(*) from LikeEntity entity where entity.post = :post")
    long countByPost(@Param("post") PostEntity postEntity);

    @Transactional
    @Modifying
    @Query("DELETE FROM LikeEntity where id = :likeId")
    void deleteLike(@Param("likeId") Long likeId);

    @Query("SELECT COUNT(e) > 0 FROM LikeEntity e WHERE e.user = :user AND e.post.id = :postId")
    boolean existsByUserAndPost(@Param("user") UserEntity user, @Param("postId") Long postId);
}
