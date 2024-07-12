package sns.snsproject.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sns.snsproject.model.User;

@Getter
@AllArgsConstructor
public
class UserResponse {
    private Long id;
    private String userName;

    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername()
        );
    }

}