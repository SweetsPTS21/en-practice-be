package com.swpts.enpracticebe.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecentActivityRequest {
    private String userName;
    private String activityType;
    private String entityName;
    private Instant from;
    private Instant to;
    private int page = 0;
    private int size = 10;
}
