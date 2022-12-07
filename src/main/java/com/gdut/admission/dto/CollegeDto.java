package com.gdut.admission.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class CollegeDto {
    // 学院名称
    private String collegeName;
    // 学院最高分
    private Double maxScore;
    // 学院最高排位
    private Integer maxRank;
    // 学院最低分
    private Double minScore;
    // 学院最低排位
    private Integer minRank;
    // 学院平均分
    private Double avgScore;
}
