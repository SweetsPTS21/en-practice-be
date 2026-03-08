package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.auth.PresignUrlResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    PresignUrlResponse presignUrl(String key, String ts, String signature);

    String uploadAudio(MultipartFile file);
}
