package sns.snsproject.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sns.snsproject.model.entity.UserEntity;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class User {

    private Long id;
    private String userName;
    private String password;
    private UserRole role;
    private Timestamp registeredAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;


    public static User fromEntity(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getUserName(),
                entity.getPassword(),
                entity.getRole(),
                entity.getRegisteredAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );

    }
}