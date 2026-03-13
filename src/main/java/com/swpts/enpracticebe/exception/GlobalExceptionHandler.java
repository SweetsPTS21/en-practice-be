package com.swpts.enpracticebe.exception;

import com.swpts.enpracticebe.dto.response.DefaultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DefaultResponse<Object> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return DefaultResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public DefaultResponse<Object> handleNotFound(NoSuchElementException ex) {
        log.warn("No such element: {}", ex.getMessage());
        return DefaultResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public DefaultResponse<Object> handleGeneral(Exception ex) {
        log.error("Unexpected error: ", ex);
        return DefaultResponse.fail("Đã có lỗi xảy ra, vui lòng thử lại sau!");
    }
}
