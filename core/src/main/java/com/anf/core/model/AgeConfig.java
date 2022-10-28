package com.anf.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgeConfig {

    private String minAge;
    private String maxAge;
}
