package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.file.AvatarUploadResponse;
import com.swpts.enpracticebe.service.FileService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/avatar")
    public ResponseEntity<DefaultResponse<AvatarUploadResponse>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(DefaultResponse.success(
                "Upload successful",
                AvatarUploadResponse.builder()
                        .avatarUrl(fileService.uploadAvatar(file))
                        .build()));
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<Object> presignUrl(@RequestParam("key") String key,
                                             @RequestHeader(value = "X-Worker-Timestamp", required = false) String ts,
                                             @RequestHeader(value = "X-Worker-Signature", required = false) String signature) {

        if (Strings.isBlank(key)) {
            return ResponseEntity.badRequest().body("missing key");
        }
        if (Strings.isBlank(ts) || Strings.isBlank(signature)) {
            return ResponseEntity.status(401).body("missing authentication headers");
        }

        return ResponseEntity.ok(fileService.presignUrl(key, ts, signature));
    }
}
