package com.gdut.admission.dto;

import com.gdut.admission.entity.Admission;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AdmissionDto extends Admission {
    private String stuName;
    private Double score;
    private String stuRank;
    private String language;
    private String professionNum;
    private String professionName;
    private String collegeName;
    private String comment;
    private String location;
}
