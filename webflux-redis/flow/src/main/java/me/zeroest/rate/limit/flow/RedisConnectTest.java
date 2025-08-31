package me.zeroest.rate.limit.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisConnectTest implements ApplicationListener<ApplicationReadyEvent> {

//    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private final ReactiveValueOperations<String, String> valueOps;

    public RedisConnectTest(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
//        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.valueOps = reactiveRedisTemplate.opsForValue();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.debug("Testing RedisConnectTest");
        valueOps.set("test-key", "test-value").subscribe();
        valueOps.get("test-key").subscribe(value -> log.debug("Received: {}", value));
    }

}
