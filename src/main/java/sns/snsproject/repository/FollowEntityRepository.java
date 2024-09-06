package sns.snsproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sns.snsproject.model.entity.FollowEntity;
import sns.snsproject.model.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface FollowEntityRepository extends JpaRepository<FollowEntity, Long> {
    // 특정 사용자가 다른 사용자를 팔로우하고 있는지 확인
    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);

    // 팔로우 관계 조회
    Optional<FollowEntity> findByFollowerAndFollowing(UserEntity follower, UserEntity following);

    // 특정 사용자가 팔로우한 사용자 목록 조회
    List<FollowEntity> findByFollower(UserEntity follower);
}
