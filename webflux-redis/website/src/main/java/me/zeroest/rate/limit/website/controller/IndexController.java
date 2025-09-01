package me.zeroest.rate.limit.website.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import me.zeroest.rate.limit.website.dto.AllowedUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

@Controller
public class IndexController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/")
    public String index(
            @RequestParam(name = "queue_name", defaultValue = "default") String queueName,
            @RequestParam(name = "user_id") Long userId,
            HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();
        String cookieName = "user-queue-%s-token".formatted(queueName);

        String token = "";
        if (cookies != null) {
            Optional<Cookie> cookie = Arrays.stream(cookies)
                    .filter(c -> c.getName().equalsIgnoreCase(cookieName))
                    .findFirst();
            token = cookie.orElse(new Cookie(cookieName, "")).getValue();
        }

        URI uri = UriComponentsBuilder.fromUriString("http://127.0.0.1:9010")
                .path("/api/v1/queue/allowed")
                .queryParam("queue_name", queueName)
                .queryParam("user_id", userId)
                .queryParam("token", token)
                .encode()
                .build()
                .toUri();

        ResponseEntity<AllowedUserResponse> responseEntity = restTemplate.getForEntity(uri, AllowedUserResponse.class);
        if (responseEntity.getBody() == null || !responseEntity.getBody().isAllowed()) {
            return "redirect:http://127.0.0.1:9010/waiting-room?user_id=%d&redirect_url=%s"
                    .formatted(userId, "http://127.0.0.1:9000?user_id=%d".formatted(userId));
        }

        // 허용된 상태라면 해당 페이지 진입
        return "index";
    }

}
