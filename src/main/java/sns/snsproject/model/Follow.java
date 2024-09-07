package sns.snsproject.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sns.snsproject.model.entity.FollowEntity;

import java.sql.Timestamp;
@Getter
@AllArgsConstructor
public class Follow {

    private Long id;
    private User follower;
    private User following;
    private Timestamp followedAt;

    public static Follow fromEntity(FollowEntity entity) {
        return new Follow(
                entity.getId(),
                User.fromEntity(entity.getFollower()),
                User.fromEntity(entity.getFollowing()),
                entity.getFollowedAt()
        );
    }
}
