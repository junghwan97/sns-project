package sns.snsproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.snsproject.controller.response.PostResponse;
import sns.snsproject.exception.ErrorCode;
import sns.snsproject.exception.SnsApplicationException;
import sns.snsproject.model.AlarmArgs;
import sns.snsproject.model.AlarmType;
import sns.snsproject.model.Comment;
import sns.snsproject.model.Post;
import sns.snsproject.model.entity.*;
import sns.snsproject.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final LikeEntityRepository likeEntityRepository;
    private final CommentEntityRepository commentEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // Redis Sorted Set의 key
    private final String REDIS_KEY = "post:views";

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

    public Page<Post> list(Pageable pageable) {
        return postEntityRepository.findAll(pageable).map(Post::fromEntity);
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
        redisTemplate.opsForZSet().incrementScore(REDIS_KEY, member, 1);
    }

    public List<PostResponse> getTopPosts(int limit) {
        // Redis Sorted Set의 key
        String key = "post:views";
        // Sorted Set에서 상위 N개 항목과 점수를 조회
        Set<ZSetOperations.TypedTuple<String>> topPosts = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);
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
        redisTemplate.delete(REDIS_KEY);
    }

    public Page<Post> my(String userName, Pageable pageable) {
        UserEntity userEntity = getUserEntityOrException(userName);
        return postEntityRepository.findAllByUser(userEntity, pageable).map(Post::fromEntity);
    }

    @Transactional
    public void like(Long postId, String userName) {

        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);

        // check like
        likeEntityRepository.findByUserAndPost(userEntity, postEntity).ifPresent(it -> {
            throw new SnsApplicationException(ErrorCode.ALREADY_LIKED, String.format("userName %s already like post %d", userName, postId));
        });

        likeEntityRepository.save(LikeEntity.of(userEntity, postEntity));

        alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_COMMENT_ON_POST, new AlarmArgs(userEntity.getId(), postEntity.getId())));
    }

    @Transactional
    public long likeCount(Long postId) {
        PostEntity postEntity = getPostEntityOrException(postId);
        return likeEntityRepository.countByPost(postEntity);
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
