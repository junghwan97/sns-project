package sns.snsproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sns.snsproject.controller.request.UserJoinRequest;
import sns.snsproject.controller.request.UserLoginRequest;
import sns.snsproject.controller.response.AlarmResponse;
import sns.snsproject.controller.response.Response;
import sns.snsproject.controller.response.UserJoinResponse;
import sns.snsproject.controller.response.UserLoginResponse;
import sns.snsproject.model.User;
import sns.snsproject.service.UserService;

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
        return Response.success(userService.alarmList(authentication.getName(), pageable).map(AlarmResponse::fromAlarm));
    }
}
