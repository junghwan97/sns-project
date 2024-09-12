package sns.snsproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sns.snsproject.model.entity.PostEntity;
import sns.snsproject.model.entity.UserEntity;

@Repository
public interface PostEntityRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findAllByUser(UserEntity userEntity, Pageable pageable);

    Page<PostEntity> findAllByOrderByRegisteredAtDesc(Pageable pageable);

    @Query("SELECT p FROM PostEntity p WHERE p.user IN " +
            "(SELECT f.following FROM FollowEntity f WHERE f.follower = :user) " +
            "ORDER BY p.registeredAt DESC")
    Page<PostEntity> findPostsFromFollowedUsers(@Param("user") UserEntity user, Pageable pageable);
}
