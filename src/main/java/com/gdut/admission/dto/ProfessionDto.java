package com.gdut.admission.dto;

import com.gdut.admission.entity.Plan;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ProfessionDto{
    // 专业代号
    private String professionNum;
    // 专业名称
    private String professionName;
    // 所属学院名称
    private String collegeName;
    // 专业最高分
    private Double maxScore;
    // 专业最高排位
    private Integer maxRank;
    // 专业最低分
    private Double minScore;
    // 专业最低排位
    private Integer minRank;
    // 专业平均分
    private Double avgScore;
}
