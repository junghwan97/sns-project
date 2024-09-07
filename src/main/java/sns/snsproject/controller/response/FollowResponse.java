package sns.snsproject.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sns.snsproject.model.Follow;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class FollowResponse {

    private Long id;
    private UserResponse follower;
    private UserResponse following;
    private Timestamp followedAt;

    public static FollowResponse fromFollow(Follow follow) {

        return new FollowResponse(
                follow.getId(),
                UserResponse.fromUser(follow.getFollower()),
                UserResponse.fromUser(follow.getFollowing()),
                follow.getFollowedAt()
        );
    }
}
