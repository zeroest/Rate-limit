package me.zeroest.rate.limit.flow.controller;

import lombok.RequiredArgsConstructor;
import me.zeroest.rate.limit.flow.dto.RegisterUserResponse;
import me.zeroest.rate.limit.flow.service.UserQueueService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

}
