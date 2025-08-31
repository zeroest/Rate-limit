package me.zeroest.rate.limit.flow.dto;

public record IsAllowedUserResponse(Long userId, Boolean isAllowed) {
}
