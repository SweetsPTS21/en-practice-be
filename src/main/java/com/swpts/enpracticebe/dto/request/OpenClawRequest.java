package com.swpts.enpracticebe.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OpenClawRequest {
    private String model;
    private String user;
    private List<Message> messages;

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}