package com.swpts.enpracticebe.dto.request.admin;

import com.swpts.enpracticebe.dto.request.PageRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFilterRequest extends PageRequest {
    private String search;
    private String role;      // "USER" | "ADMIN" | null for all
    private Boolean isActive; // null for all
}
