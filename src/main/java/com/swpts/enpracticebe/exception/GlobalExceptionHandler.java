package com.swpts.enpracticebe.exception;

import com.swpts.enpracticebe.dto.response.DefaultResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public DefaultResponse<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return DefaultResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public DefaultResponse<Object> handleNotFound(NoSuchElementException ex) {
        return DefaultResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public DefaultResponse<Object> handleGeneral(Exception ex) {
        return DefaultResponse.fail("Đã có lỗi xảy ra, vui lòng thử lại sau!");
    }
}
