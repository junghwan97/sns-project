package sns.snsproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sns.snsproject.model.entity.PostEntity;
import sns.snsproject.model.entity.UserEntity;

import java.util.List;

@Repository
public interface PostEntityRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findAllByUser(UserEntity userEntity, Pageable pageable);

    Page<PostEntity> findAllByOrderByRegisteredAtDesc(Pageable pageable);

    @Query("SELECT p FROM PostEntity p " +
            "JOIN FollowEntity f ON p.user = f.following " +
            "WHERE f.follower = :user " +
            "ORDER BY p.id DESC")
    List<PostEntity> findPostsByFollowerOrderByIdDescWithoutCursor(UserEntity user, Pageable pageable);

    @Query("SELECT p FROM PostEntity p " +
            "JOIN FollowEntity f ON p.user = f.following " +
            "WHERE f.follower = :user AND p.id < :cursorId " +
            "ORDER BY p.id DESC")
    List<PostEntity> findPostsByFollowerOrderByIdDesc(UserEntity user, Pageable pageable, Long cursorId);
}
