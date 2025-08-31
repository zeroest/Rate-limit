package me.zeroest.rate.limit.flow.service;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static me.zeroest.rate.limit.flow.exception.ErrorCode.QUEUE_ALREADY_REGISTERED_USER;

@Service
public class UserQueueService {

    private static final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
    private static final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";

//    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private final ReactiveZSetOperations<String, String> reactiveZSetOperations;


    public UserQueueService(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
//        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.reactiveZSetOperations = reactiveRedisTemplate.opsForZSet();
    }

    // redis sortedset
    // - key: userId
    // - value: unix timestamp
    public Mono<Long> registerWaitQueue(final String queueName, final Long userId) {
        final long unixTimestamp = System.currentTimeMillis();
        final String userQueueWaitKey = USER_QUEUE_WAIT_KEY.formatted(queueName);

        return reactiveZSetOperations.add(userQueueWaitKey, userId.toString(), unixTimestamp)
                .filter(isRegistered -> isRegistered) // 이미 존재하는 userId 는 return false
                .switchIfEmpty(Mono.error(QUEUE_ALREADY_REGISTERED_USER.build(queueName)))
                .flatMap(isRegistered -> reactiveZSetOperations.rank(userQueueWaitKey, userId.toString()))
                .map(rank -> rank >= 0 ? rank + 1 : rank);
    }

    // 진입을 허용
    public Mono<Long> allowUser(final String queueName, final Long count) {
        // 1. wait queue 사용자 제거
        // 2. proceed queue 사용자 추가
        final long unixTimestamp = System.currentTimeMillis();
        final String userQueueWaitKey = USER_QUEUE_WAIT_KEY.formatted(queueName);
        final String userQueueProceedKey = USER_QUEUE_PROCEED_KEY.formatted(queueName);

        return reactiveZSetOperations.popMin(userQueueWaitKey, count)
                // tuple[score, value] => [unixTimestamp, userId]
                .flatMap(tuple -> reactiveZSetOperations.add(userQueueProceedKey, tuple.getValue(), unixTimestamp))
                // 요청된건 5개지만 실제 wait queue 에 3명의 유저가 남아 있으면 3이 반환
                .count();
    }

    // 진입이 가능한 상태인지 조회
    public Mono<Boolean> isAllowed(final String queueName, final Long userId) {
        final String userQueueProceedKey = USER_QUEUE_PROCEED_KEY.formatted(queueName);

        return reactiveZSetOperations.rank(userQueueProceedKey, userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0);
    }

    // 대기 번호 조회
    public Mono<Long> getRank(final String queueName, final Long userId) {
        final String userQueueWaitKey = USER_QUEUE_WAIT_KEY.formatted(queueName);

        return reactiveZSetOperations.rank(userQueueWaitKey, userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0 ? rank + 1 : rank);
    }

}
