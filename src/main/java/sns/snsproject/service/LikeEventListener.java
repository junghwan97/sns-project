package sns.snsproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sns.snsproject.model.AlarmArgs;
import sns.snsproject.model.AlarmType;
import sns.snsproject.model.LikeEvent;
import sns.snsproject.model.entity.AlarmEntity;
import sns.snsproject.model.entity.LikeEntity;
import sns.snsproject.model.entity.PostEntity;
import sns.snsproject.model.entity.UserEntity;
import sns.snsproject.repository.AlarmEntityRepository;
import sns.snsproject.repository.LikeEntityRepository;
import sns.snsproject.repository.PostEntityRepository;
import sns.snsproject.repository.UserEntityRepository;

@Component
@RequiredArgsConstructor
public class LikeEventListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final UserEntityRepository userEntityRepository;
    private final PostEntityRepository postEntityRepository;
    private final LikeEntityRepository likeEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;

    private static final String LIKE_KEY_FORMAT = "post:%d:likes";

    @KafkaListener(topics = "like-topic", groupId = "like-consumer")
    @Transactional
    public void consume(String message) {
        try {
            // 1. Kafka 메시지 역직렬화
            LikeEvent event = objectMapper.readValue(message, LikeEvent.class);

            Long postId = event.getPostId();
            String userName = event.getUserName();

            // 2. 사용자, 게시글 조회
            UserEntity user = userEntityRepository.findByUserName(userName)
                    .orElseThrow(() -> new RuntimeException("사용자 없음"));
            PostEntity post = postEntityRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("게시글 없음"));

            // 3. 중복 좋아요 검사
            boolean alreadyLiked = likeEntityRepository.existsByUserAndPost(user, postId);
            if (alreadyLiked) {
                likeEntityRepository.deleteByUserAndPost(user, postId);
            } else {
                // 4. 좋아요 저장
                likeEntityRepository.save(LikeEntity.of(user, post));

                // 5. 알람 저장
                alarmEntityRepository.save(AlarmEntity.of(
                        post.getUser(),
                        AlarmType.NEW_LIKE_ON_POST,
                        new AlarmArgs(user.getId(), post.getId())
                ));
            }


            // 6. Redis → DB 동기화
            String redisKey = String.format(LIKE_KEY_FORMAT, postId);
            String likeCountStr = redisTemplate.opsForValue().get(redisKey);
            int likeCount = likeCountStr != null && Integer.parseInt(likeCountStr) > 0 ? Integer.parseInt(likeCountStr) : 0;

            post.setLikeCount(likeCount);
            postEntityRepository.save(post);

        } catch (Exception e) {
            // 예외 로깅
            System.err.println("❌ Kafka 메시지 처리 실패: " + e.getMessage());
        }
    }
}
