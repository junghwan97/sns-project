package sns.snsproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.snsproject.controller.response.PostResponse;
import sns.snsproject.exception.ErrorCode;
import sns.snsproject.exception.SnsApplicationException;
import sns.snsproject.model.*;
import sns.snsproject.model.entity.*;
import sns.snsproject.repository.*;
import sns.snsproject.util.RedisStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final LikeEntityRepository likeEntityRepository;
    private final CommentEntityRepository commentEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final RedisStorage redisStorage;

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // Redis Sorted Set의 key
    private static final String REDIS_KEY = "post:views";

    @Transactional
    public void create(String title, String body, String userName) {
        // user 조회 + user가 없다면
        UserEntity userEntity = getUserEntityOrException(userName);
        // post save
        postEntityRepository.save(PostEntity.of(title, body, userEntity));
    }

    @Transactional
    public Post modify(String title, String body, String userName, Long postId) {
        // user 조회 + user가 없다면
        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);

        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", userName, postId));
        }

        postEntity.setTitle(title);
        postEntity.setBody(body);
        return Post.fromEntity(postEntityRepository.saveAndFlush(postEntity));
    }

    @Transactional
    public void delete(String userName, Long postId) {
        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);

        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", userName, postId));
        }
        postEntityRepository.delete(postEntity);
    }

    @Cacheable(cacheNames = "getPosts", key = "'posts:page:' + #page + ':size:' + #size", cacheManager = "postCacheManager")
    public List<PostResponse> getPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostEntity> pageOfBoards = postEntityRepository.findAllByOrderByRegisteredAtDesc(pageable);
        List<PostResponse> postResponses = new ArrayList<>();
        for (PostEntity postEntity : pageOfBoards) {
            postResponses.add(PostResponse.fromPost(Post.fromEntity(postEntity)));
        }
        return postResponses;
    }

    public Post selectById(Long postId) {
        PostEntity postEntity = getPostEntityOrException(postId);
        incrementPostView(postId);
        return Post.fromEntity(postEntity);
    }

    public void incrementPostView(Long postId) {
        // 게시글 ID를 value로 사용
        String member = postId.toString();
        // 조회수를 1 증가시키기
        redisStorage.incrementScore(REDIS_KEY, member);
    }

    @Cacheable(cacheNames = "popularPosts", key = "'posts:'", cacheManager = "postCacheManager")
    public List<PostResponse> getTopPosts(int limit) {
        // Sorted Set에서 상위 N개 항목과 점수를 조회
        Set<ZSetOperations.TypedTuple<String>> topPosts = redisStorage.getTopPostsWithScores(REDIS_KEY, limit);
        List<PostResponse> topPostResponses = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : topPosts) {
            String postId = tuple.getValue();
            // 게시글을 데이터베이스에서 조회
            PostEntity postEntity = postEntityRepository.findById(Long.parseLong(postId))
                    .orElseThrow(() -> new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("%s not founded", postId)));

            // 게시글 응답 객체로 변환
            topPostResponses.add(PostResponse.fromPost(Post.fromEntity(postEntity)));
        }

        return topPostResponses;
    }

    // 자정마다 조회수 초기화
    @Scheduled(cron = "0 0 0 * * *")
    public void resetViews() {
        redisStorage.delete(REDIS_KEY);
    }

    public Page<Post> my(String userName, Pageable pageable) {
        UserEntity userEntity = getUserEntityOrException(userName);
        return postEntityRepository.findAllByUser(userEntity, pageable).map(Post::fromEntity);
    }

    public void likeWithRetry(Long postId, String userName) {
        int retry = 0;
        long startTime = System.currentTimeMillis();
        while (retry < 10) {
            try {
                like(postId, userName);
                long endTime = System.currentTimeMillis();
                logSuccess(userName, retry, endTime - startTime);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                retry++;
                System.out.println("🔁 낙관적 락 재시도 #" + retry);
            } catch (SnsApplicationException e) {
                long endTime = System.currentTimeMillis();
                logFailure(userName, retry, endTime - startTime, e.getMessage());
                return;
            }
        }
//        throw new SnsApplicationException(ErrorCode.CONFLICT_LIKE, "동시 요청 충돌, 나중에 다시 시도해주세요.");
        long endTime = System.currentTimeMillis();
        logFailure(userName, retry, endTime - startTime, "재시도 초과");
    }

    private void logSuccess(String userName, int retry, long durationMs) {
        System.out.printf("[Thread-%s] ✅ 성공 | 재시도: %d회 | 소요시간: %dms%n", userName, retry, durationMs);
    }

    private void logFailure(String userName, int retry, long durationMs, String reason) {
        System.out.printf("[Thread-%s] ❌ 실패 | 재시도: %d회 | 소요시간: %dms | 예외: %s%n", userName, retry, durationMs, reason);
    }

    @Transactional
    public void like(Long postId, String userName) {

        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);

        // 불필요한 데이터 로딩을 줄여 성능과 명확성을 높이기 위해 리팩토링
        boolean alreadyLiked = likeEntityRepository.existsByUserAndPost(userEntity, postId);
        if (alreadyLiked) {
            throw new SnsApplicationException(ErrorCode.ALREADY_LIKED, String.format("userName %s already like post %d", userName, postId));
        }

        postEntity.incrementLikeCount();
        postEntityRepository.saveAndFlush(postEntity);
        likeEntityRepository.save(LikeEntity.of(userEntity, postEntity));
        alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_COMMENT_ON_POST, new AlarmArgs(userEntity.getId(), postEntity.getId())));
    }

    private static final String LIKE_KEY_FORMAT = "post:%d:likes";

    public void likes(Long postId, String userName) {
        // Redis 키 구성: post:{postId}:likes
        String redisKey = String.format(LIKE_KEY_FORMAT, postId);

        // 좋아요 중복 시 좋아요 개수 감소
        boolean alreadyLiked = likeEntityRepository.existsByUserAndPost(getUserEntityOrException(userName), postId);
        if (alreadyLiked) {
            redisTemplate.opsForValue().decrement(redisKey);
        } else {
            // Redis에서 좋아요 수 증가
            redisTemplate.opsForValue().increment(redisKey);
        }


        // 2. Kafka 이벤트 전송
        LikeEvent event = new LikeEvent(postId, userName);
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("like-topic", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kafka 메시지 직렬화 실패", e);
        }
    }

    @Transactional
    public long likeCount(Long postId) {
//        PostEntity postEntity = getPostEntityOrException(postId);
//        return likeEntityRepository.countByPost(postEntity);
        String key = String.format("post:%d:likes", postId);
        String countStr = redisTemplate.opsForValue().get(key);

        if (countStr != null) {
            return Integer.parseInt(countStr);
        }

        // Redis에 없을 경우 DB fallback (선택적)
        return postEntityRepository.findById(postId)
                .map(PostEntity::getLikeCount)
                .orElse(0);
    }

    @Transactional
    public void comment(Long postId, String comment, String userName) {
        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);

        commentEntityRepository.save(CommentEntity.of(userEntity, postEntity, comment));
        alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_COMMENT_ON_POST, new AlarmArgs(userEntity.getId(), postEntity.getId())));

    }

    public Page<Comment> getComment(Long postId, Pageable pageable) {
        PostEntity postEntity = getPostEntityOrException(postId);
        return commentEntityRepository.findAllByPost(postEntity, pageable).map(Comment::fromEntity);
    }

    public List<PostResponse> getPostsFromFollowedUsers(String userName, Integer pageSize, Long cursorId) {
        UserEntity user = getUserEntityOrException(userName);
        pageSize = pageSize + 1;

        List<PostEntity> postEntities = new ArrayList<>();
        if (cursorId == 0) {
            postEntities = postEntityRepository.findPostsByFollowerOrderByIdDescWithoutCursor(user, pageSize);
        } else {
            postEntities = postEntityRepository.findPostsByFollowerOrderByIdDesc(user, cursorId, pageSize);
        }
        boolean hasNext = hasNext(postEntities.size(), pageSize);
        postEntities = toSubListIfHasNext(hasNext, pageSize, postEntities);
        List<PostResponse> postResponses = new ArrayList<>();
        List<Post> posts = postEntities.stream().map(Post::fromEntity).collect(Collectors.toList());
        for (Post post : posts) {
            postResponses.add(PostResponse.fromPost(post));
        }
        return postResponses;
    }

    private boolean hasNext(int postsSize, int pageSize) {
        if (postsSize == 0) {
            throw new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("Next page isn't exist"));
        }
        return postsSize > pageSize;
    }

    private List<PostEntity> toSubListIfHasNext(boolean hasNext, int pageSize, List<PostEntity> posts) {
        return hasNext ? posts.subList(0, pageSize) : posts;
    }

    //post exist
    private PostEntity getPostEntityOrException(Long postId) {
        return postEntityRepository.findById(postId).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("%s not founded", postId)));
    }


    // user exist
    private UserEntity getUserEntityOrException(String userName) {
        return userEntityRepository.findByUserName(userName).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));
    }
}
