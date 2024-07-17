package sns.snsproject.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sns.snsproject.model.entity.PostEntity;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class Post {

    private Long id;

    private String title;

    private String body;

    private User user;

    private Timestamp registeredAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;

    public static Post fromEntity(PostEntity entity) {
        return new Post(
                entity.getId(),
                entity.getTitle(),
                entity.getBody(),
                User.fromEntity(entity.getUser()),
                entity.getRegisteredAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
