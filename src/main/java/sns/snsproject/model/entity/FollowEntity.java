package sns.snsproject.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "\"follow\"")
@Getter
@Setter
public class FollowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private UserEntity follower;

    @ManyToOne
    @JoinColumn(name = "following_id")
    private UserEntity following;

    @Column(name = "followed_at")
    private Timestamp followedAt;

    @PrePersist
    void followedAt() {
        this.followedAt = Timestamp.from(Instant.now());
    }

    public static FollowEntity of(UserEntity follower, UserEntity following) {
        FollowEntity entity = new FollowEntity();
        entity.setFollower(follower);
        entity.setFollowing(following);
        return entity;
    }
}