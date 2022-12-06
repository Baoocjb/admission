package com.gdut.admission.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SortPlan {
    private Integer planId;
    private Double minScore;
}
