package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.response.AiExplainResponse;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.service.AiExplainService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/claw-bot")
@AllArgsConstructor
public class AiExplainController {
    private final AiExplainService aiExplainService;

    @GetMapping("/explain")
    public DefaultResponse<AiExplainResponse> explainWord(
            @RequestParam String word) {
        try {
            return DefaultResponse.success(aiExplainService.askAI(word));
        } catch (Exception e) {
            return DefaultResponse.fail("Không thể kết nối AI: " + e.getMessage());
        }
    }
}
