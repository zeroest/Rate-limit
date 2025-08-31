package me.zeroest.rate.limit.flow.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public class ApplicationException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String code;
    private final String reason;
}
