package sns.snsproject.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sns.snsproject.model.entity.CommentEntity;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class Comment {

    private Long id;

    private String comment;

    private String userName;

    private Long postId;

    private Timestamp registeredAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;

    public static Comment fromEntity(CommentEntity entity) {
        return new Comment(
                entity.getId(),
                entity.getComment(),
                entity.getUser().getUserName(),
                entity.getPost().getId(),
                entity.getRegisteredAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}