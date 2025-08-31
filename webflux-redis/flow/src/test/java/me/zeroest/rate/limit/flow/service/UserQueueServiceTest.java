package me.zeroest.rate.limit.flow.service;

import me.zeroest.rate.limit.flow.EmbeddedRedis;
import me.zeroest.rate.limit.flow.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(EmbeddedRedis.class)
class UserQueueServiceTest {

    @Autowired
    private UserQueueService userQueueService;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @BeforeEach
    void beforeEach() {
        ReactiveRedisConnection reactiveRedisConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        reactiveRedisConnection.serverCommands().flushAll().subscribe();
    }

    @Test
    void registerWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 102L))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 103L))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void alreadyRegisteredWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L))
                .expectNext(1L)
                .verifyComplete();
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L))
                .expectError(ApplicationException.class)
                .verify();
    }

    @Test
    void emptyAllowedUser() {
        StepVerifier.create(userQueueService.allowUser("default", 2L))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void allowUser() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 101L)
                                .then(userQueueService.registerWaitQueue("default", 102L))
                                .then(userQueueService.registerWaitQueue("default", 103L))
                )
                .expectNext(3L)
                .verifyComplete();

        StepVerifier.create(userQueueService.allowUser("default", 2L))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(userQueueService.allowUser("default", 2L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.allowUser("default", 2L))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void allowUserAfterRegisterWaitQueue() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 101L)
                                .then(userQueueService.registerWaitQueue("default", 102L))
                                .then(userQueueService.registerWaitQueue("default", 103L))
                                .then(userQueueService.allowUser("default", 3L))
                                .then(userQueueService.registerWaitQueue("default", 104L))
                )
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void isNotAllowed1() {
        StepVerifier.create(userQueueService.isAllowed("default", 101L))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isNotAllowed2() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 101L)
                                .then(userQueueService.allowUser("default", 3L))
                                .then(userQueueService.isAllowed("default", 102L))
                )
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isAllowed() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 101L)
                                .then(userQueueService.registerWaitQueue("default", 102L))
                                .then(userQueueService.registerWaitQueue("default", 103L))
                                .then(userQueueService.allowUser("default", 3L))
                                .then(userQueueService.isAllowed("default", 101L))
                )
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(userQueueService.isAllowed("default", 102L))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(userQueueService.isAllowed("default", 103L))
                .expectNext(true)
                .verifyComplete();
    }

}