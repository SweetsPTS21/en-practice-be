package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.service.FileService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

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
