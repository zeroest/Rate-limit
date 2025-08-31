package me.zeroest.rate.limit.flow.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ApplicationAdvice {

    @ExceptionHandler(ApplicationException.class)
    Mono<ResponseEntity<ServerExceptionResponse>> applicationException(ApplicationException ex) {
        ResponseEntity<ServerExceptionResponse> responseEntity = ResponseEntity
                .status(ex.getHttpStatus())
                .body(new ServerExceptionResponse(ex.getCode(), ex.getReason()));
        return Mono.just(responseEntity);
    }

    public record ServerExceptionResponse(String code, String reason) {
    }

}
