package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.PresignUrlResponse;

public interface FileService {
    PresignUrlResponse presignUrl(String key, String ts, String signature);
}
