package com.swpts.enpracticebe.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest {
    private Integer page = 0;
    private Integer size = 10;
}
