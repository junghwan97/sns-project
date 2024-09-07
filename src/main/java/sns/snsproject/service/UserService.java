package sns.snsproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.snsproject.exception.ErrorCode;
import sns.snsproject.exception.SnsApplicationException;
import sns.snsproject.model.Alarm;
import sns.snsproject.model.Follow;
import sns.snsproject.model.User;
import sns.snsproject.model.entity.FollowEntity;
import sns.snsproject.model.entity.UserEntity;
import sns.snsproject.repository.AlarmEntityRepository;
import sns.snsproject.repository.FollowEntityRepository;
import sns.snsproject.repository.UserEntityRepository;
import sns.snsproject.util.JwtTokenUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserEntityRepository userEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final FollowEntityRepository followEntityRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private Long expiredTimeMs;

    @Transactional
    public User join(String userName, String password) {
        // 회원가입하려는 userName으로 회원가입된 user가 있는지
        userEntityRepository.findByUserName(userName).ifPresent(it -> {
            throw new SnsApplicationException(ErrorCode.DUPLICATE_USER_NAME, String.format("%s is duplicated", userName));
        });

        // 회원가입 진행 = user등록
        UserEntity userEntity = userEntityRepository.save(UserEntity.of(userName, encoder.encode(password)));
        return User.fromEntity(userEntity);
    }

    public String login(String userName, String password) {
        //회원 가입 여부 체크
        UserEntity userEntity = userEntityRepository.findByUserName(userName).orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));
        // 비밀번호 체크
        if (!encoder.matches(password, userEntity.getPassword())) {

            throw new SnsApplicationException(ErrorCode.INVALID_PASSWORD);
        }
        // 토큰 생성
        String token = JwtTokenUtils.generateToken(userName, secretKey, expiredTimeMs);

        return token;
    }

    public Page<Alarm> alarmList(Long userId, Pageable pageable) {
        return alarmEntityRepository.findAllByUserId(userId, pageable).map(Alarm::fromEntity);
    }

    public User loadUserByUsername(String userName) {
        return userEntityRepository.findByUserName(userName).map(User::fromEntity).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName))
        );
    }

    @Transactional
    public Follow follow(Long followerId, Long followingId) {
        UserEntity follower = getUserEntityOrException(followerId);
        UserEntity following = getUserEntityOrException(followingId);

        validateNotFollowing(follower, following);

        FollowEntity followEntity = FollowEntity.of(follower, following);
        followEntityRepository.save(followEntity);
        return Follow.fromEntity(followEntity);
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        UserEntity follower = getUserEntityOrException(followerId);
        UserEntity following = getUserEntityOrException(followingId);

        FollowEntity followEntity =validateFollowExists(follower, following);
        followEntityRepository.delete(followEntity);
    }

    // user exist
    private UserEntity getUserEntityOrException(Long userId) {
        return userEntityRepository.findById(userId)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, "Follower not found"));
    }

    // follow exist
    private void validateNotFollowing(UserEntity follower, UserEntity following) {
        if (followEntityRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new SnsApplicationException(ErrorCode.DUPLICATE_FOLLOW, "Already following");
        }
    }

    // follow exist for Unfollow
    private FollowEntity validateFollowExists(UserEntity follower, UserEntity following) {
        return followEntityRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.FOLLOW_NOT_FOUND, "Follow relationship not found"));
    }
}
