package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.response.AiAskResponse;
import com.swpts.enpracticebe.dto.response.AiExplainResponse;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.service.OpenClawService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/open-claw")
@AllArgsConstructor
public class AiExplainController {
    private final OpenClawService openClawService;

    @GetMapping("/explain")
    public DefaultResponse<AiExplainResponse> explainWord(
            @RequestParam String word) {
        try {
            return DefaultResponse.success(openClawService.explainWord(word));
        } catch (Exception e) {
            return DefaultResponse.fail("Không thể kết nối AI: " + e.getMessage());
        }
    }

    @GetMapping("/ask")
    public DefaultResponse<AiAskResponse> askAi(
            @RequestParam String prompt) {
        try {
            return DefaultResponse.success(openClawService.askAi(prompt));
        } catch (Exception e) {
            return DefaultResponse.fail("Không thể kết nối AI: " + e.getMessage());
        }
    }
}
