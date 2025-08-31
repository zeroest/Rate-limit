package me.zeroest.rate.limit.flow.controller;

import lombok.RequiredArgsConstructor;
import me.zeroest.rate.limit.flow.dto.AllowUserResponse;
import me.zeroest.rate.limit.flow.dto.IsAllowedUserResponse;
import me.zeroest.rate.limit.flow.dto.RegisterUserResponse;
import me.zeroest.rate.limit.flow.service.UserQueueService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {

    private final UserQueueService userQueueService;

    // 대기열 등록 API
    @PostMapping("")
    public Mono<RegisterUserResponse> registerUser(
            @RequestParam(name = "queue_name", defaultValue = "default") String queueName,
            @RequestParam(name = "user_id") Long userId
    ) {
        return userQueueService.registerWaitQueue(queueName, userId)
                .map(RegisterUserResponse::new);
    }

    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(
            @RequestParam(name = "queue_name", defaultValue = "default") String queueName,
            @RequestParam(name = "request_count") Long requestCount
    ) {
        return userQueueService.allowUser(queueName, requestCount)
                .map(allowedCount -> new AllowUserResponse(requestCount, allowedCount));
    }

    @GetMapping("/allowed")
    public Mono<IsAllowedUserResponse> isAllowedUser(
            @RequestParam(name = "queue_name", defaultValue = "default") String queueName,
            @RequestParam(name = "user_id") Long userId
    ) {
        return userQueueService.isAllowed(queueName, userId)
                .map(isAllowed -> new IsAllowedUserResponse(userId, isAllowed));
    }

}
