package sns.snsproject.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisStorage {
    private final RedisTemplate<String, String> redisTemplate;

    public void incrementScore(String REDIS_KEY, String member) {
        redisTemplate.opsForZSet().incrementScore(REDIS_KEY, member, 1);
    }

    public Set<ZSetOperations.TypedTuple<String>> getTopPostsWithScores(String REDIS_KEY, int limit) {
        Set<ZSetOperations.TypedTuple<String>> topPosts = redisTemplate.opsForZSet().reverseRangeWithScores(REDIS_KEY, 0, limit - 1);
        return topPosts;
    }

    public void delete(String REDIS_KEY){
        redisTemplate.delete(REDIS_KEY);
    }
}
