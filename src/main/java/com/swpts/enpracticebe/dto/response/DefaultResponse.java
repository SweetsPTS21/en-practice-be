package com.swpts.enpracticebe.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public DefaultResponse() {
    }

    public DefaultResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> DefaultResponse<T> success(T data) {
        return new DefaultResponse<>(true, "OK", data);
    }

    public static <T> DefaultResponse<T> success(String message) {
        return new DefaultResponse<>(true, message, null);
    }

    public static <T> DefaultResponse<T> success(String message, T data) {
        return new DefaultResponse<>(true, message, data);
    }

    public static <T> DefaultResponse<T> fail(String message) {
        return new DefaultResponse<>(false, message, null);
    }
}
