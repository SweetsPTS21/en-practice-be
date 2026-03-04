package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.constant.Constants;
import com.swpts.enpracticebe.constant.S3Properties;
import com.swpts.enpracticebe.dto.response.PresignUrlResponse;
import com.swpts.enpracticebe.service.FileService;
import com.swpts.enpracticebe.util.SignatureUtil;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;

    /**
     * Get backblaze presigned URL
     * Cached by Caffeine (10 min TTL, configured in CacheConfig)
     *
     * @param key       path to object
     * @param ts        timestamp
     * @param signature signature
     * @return PresignUrlResponse
     */
    @Override
    @Cacheable(value = "presignUrl", key = "#key")
    public PresignUrlResponse presignUrl(String key, String ts, String signature) {
        checkSignature(key, ts, signature);
        Integer time = Constants.PRESIGN_URL_TIME;

        String url = generatePresignedUrl(key, Duration.ofSeconds(time));
        return PresignUrlResponse.builder()
                .url(url)
                .expires(time)
                .build();
    }

    private String generatePresignedUrl(String key, Duration validFor) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(validFor)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private void checkSignature(String key, String ts, String signature) {
        long timestampSec;
        try {
            timestampSec = Long.parseLong(ts);
        } catch (NumberFormatException ex) {
            throw new ValidationException("invalid timestamp");
        }
        long now = System.currentTimeMillis() / 1000L;
        if (Math.abs(now - timestampSec) > Constants.SIGNATURE_SKEW_SECONDS) {
            throw new ValidationException("timestamp skew too large");
        }

        // 2) compute expected HMAC
        String message = SignatureUtil.buildMessage(key, ts);
        String expected = SignatureUtil.computeHmacHex(s3Properties.getWorkerSharedSecret(), message);

        // 3) constant-time compare
        if (!SignatureUtil.constantTimeEquals(expected, signature)) {
            throw new ValidationException("invalid signature");
        }
    }
}
