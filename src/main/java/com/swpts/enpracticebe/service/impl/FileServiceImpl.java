package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.constant.Constants;
import com.swpts.enpracticebe.constant.S3Properties;
import com.swpts.enpracticebe.dto.response.auth.PresignUrlResponse;
import com.swpts.enpracticebe.service.FileService;
import com.swpts.enpracticebe.util.SignatureUtil;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;
    private static final String CDN_FILE_BASE_URL = "https://cdn.swpts.site/files/";
    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

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

    @Override
    public String uploadAudio(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Audio file is required");
        }

        String extension = "webm";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        }

        String key = "speaking/" + UUID.randomUUID() + "." + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            putObject(file, putRequest);
            log.info("Uploaded audio to S3: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Failed to upload audio to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload audio file: " + e.getMessage());
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        validateAvatar(file);

        String extension = resolveAvatarExtension(file);
        String key = "avatars/" + UUID.randomUUID() + "." + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            putObject(file, putRequest);
            log.info("Uploaded avatar to S3: {}", key);
            return buildCdnFileUrl(key);
        } catch (Exception e) {
            log.error("Failed to upload avatar to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
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

        String message = SignatureUtil.buildMessage(key, ts);
        String expected = SignatureUtil.computeHmacHex(s3Properties.getWorkerSharedSecret(), message);

        if (!SignatureUtil.constantTimeEquals(expected, signature)) {
            throw new ValidationException("invalid signature");
        }
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar image is required");
        }
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new IllegalArgumentException("Avatar image must be smaller than 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        resolveAvatarExtension(file);
    }

    private String resolveAvatarExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Avatar image must have a valid file extension");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only jpg, jpeg, png, webp, and gif images are allowed");
        }

        return extension;
    }

    private void putObject(MultipartFile file, PutObjectRequest putRequest) throws Exception {
        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    private String buildCdnFileUrl(String key) {
        return CDN_FILE_BASE_URL + key;
    }
}
