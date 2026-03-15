package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.mascot.MascotResponse;
import com.swpts.enpracticebe.service.MascotService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class MascotController {

    private final MascotService mascotService;

    @GetMapping("/mascot")
    public DefaultResponse<MascotResponse> getMascotData() {
        return DefaultResponse.success(mascotService.getMascotData());
    }
}
