package com.gdut.admission.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class SchoolDto {
    // 学校最高分
    private Double maxScore;
    // 学校最高排位
    private Integer maxRank;
    // 学校最低分
    private Double minScore;
    // 学校最低排位
    private Integer minRank;
    // 学校平均分
    private Double avgScore;
    // 学校中位数
    private Double midScore;
}
