package me.zeroest.rate.limit.flow.controller;

import lombok.RequiredArgsConstructor;
import me.zeroest.rate.limit.flow.exception.ApplicationException;
import me.zeroest.rate.limit.flow.service.UserQueueService;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static me.zeroest.rate.limit.flow.exception.ErrorCode.QUEUE_ALREADY_REGISTERED_USER;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {

    private final UserQueueService userQueueService;

    @GetMapping("/waiting-room")
    Mono<Rendering> waitingRoomPage(
            @RequestParam(name = "queue_name", defaultValue = "default") String queueName,
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "redirect_url") String redirectUrl,
            ServerWebExchange exchange
    ) {
        String key = "user-queue-%s-token".formatted(queueName);
        HttpCookie cookieValue = exchange.getRequest().getCookies().getFirst(key);
        String token = Objects.isNull(cookieValue) ? "" : cookieValue.getValue();

        // 1. 입장이 허용되어  page redirect 가능한 상태인가?
        return userQueueService.isAllowedByToken(queueName, userId, token)
                .filter(allowed -> allowed)
                // 2. 어디로 이동해야 하는가?
                .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
                // 대기 등록
                // 웹페이지에 필요한 데이터를 전달
                .switchIfEmpty(
                        userQueueService.getRank(queueName, userId)
                                .filter(rank -> rank >= 0)
                                // 등록
                                .switchIfEmpty(
                                        userQueueService.registerWaitQueue(queueName, userId)
                                                .onErrorResume(ex -> {
                                                    if (
                                                            ex instanceof ApplicationException applicationException
                                                                    && QUEUE_ALREADY_REGISTERED_USER.getCode().equals(applicationException.getCode())
                                                    ) {
                                                        return userQueueService.getRank(queueName, userId);
                                                    }
                                                    return Mono.error(ex);
                                                })
                                )
                                .map(rank -> Rendering.view("waiting-room.html")
                                        .modelAttribute("number", rank)
                                        .modelAttribute("userId", userId)
                                        .modelAttribute("queue", queueName)
                                        .build()
                                )
                        );

/*
                        userQueueService.registerWaitQueue(queueName, userId)
                                .onErrorResume(ex -> {
                                    // 이미 등록된 사용자
//                                    if (
//                                            ex instanceof ApplicationException applicationException
//                                                    && QUEUE_ALREADY_REGISTERED_USER.getCode().equals(applicationException.getCode())
//                                    ) {
                                        return userQueueService.getRank(queueName, userId);
//                                    }
//                                    return Mono.error(ex);
                                })
                                .map(rank ->
                                        Rendering.view("waiting-room.html")
                                                .modelAttribute("number", rank)
                                                .modelAttribute("userId", userId)
                                                .modelAttribute("queue", queueName)
                                                .build()
                                )
                );
*/

//        Rendering rendering = Rendering.view("waiting-room.html").build();
//        return Mono.just(rendering);
    }

}
