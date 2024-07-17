package sns.snsproject.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sns.snsproject.model.User;
import sns.snsproject.model.UserRole;


@Getter
@AllArgsConstructor
public class UserJoinResponse {

    private Long id;
    private String userName;
    private UserRole role;

    public static UserJoinResponse fromUser(User user) {

        return new UserJoinResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}