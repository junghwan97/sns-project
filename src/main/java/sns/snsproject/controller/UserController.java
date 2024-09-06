package sns.snsproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sns.snsproject.controller.request.UserJoinRequest;
import sns.snsproject.controller.request.UserLoginRequest;
import sns.snsproject.controller.response.*;
import sns.snsproject.exception.ErrorCode;
import sns.snsproject.exception.SnsApplicationException;
import sns.snsproject.model.Follow;
import sns.snsproject.model.User;
import sns.snsproject.service.UserService;
import sns.snsproject.util.ClassUtils;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody UserJoinRequest request) {
        User user = userService.join(request.getUserName(), request.getPassword());
        return Response.success(UserJoinResponse.fromUser(user));
    }

    @PostMapping("/login")
    public Response<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        String toekn = userService.login(request.getUserName(), request.getPassword());
        return Response.success(new UserLoginResponse(toekn));
    }

    @GetMapping("/alarm")
    public Response<Page<AlarmResponse>> alarm(Pageable pageable, Authentication authentication) {
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class).orElseThrow(
                () -> new SnsApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, String.format("Casting to User class failed")));
        return Response.success(userService.alarmList(user.getId(), pageable).map(AlarmResponse::fromAlarm));
    }

    @PostMapping("/{followingId}/follow")
    public Response<FollowResponse> follow(@PathVariable Long followingId, Authentication authentication) {
        User follower = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class).orElseThrow(
                () -> new SnsApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, "User casting failed"));
        Follow follow = userService.follow(follower.getId(), followingId);
        return Response.success(FollowResponse.fromFollow(follow));
    }

    @DeleteMapping("/{followingId}/unfollow")
    public Response<String> unfollow(@PathVariable Long followingId, Authentication authentication) {
        User follower = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, "User casting failed"));
        userService.unfollow(follower.getId(), followingId);
        return Response.success("Unfollowed successfully");
    }

}
