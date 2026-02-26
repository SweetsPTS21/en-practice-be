package com.swpts.enpracticebe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSessionRequest {

    @NotBlank
    private String filter;

    private int total;
    private int correct;
    private int incorrect;
    private int accuracy;

    @NotNull
    private List<Map<String, Object>> words = new ArrayList<>();
}
