package me.zeroest.rate.limit.flow.dto;

public record AllowUserResponse(Long requestCount, Long allowedCount) {
}
