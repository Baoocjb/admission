package com.gdut.admission.dto;

import com.gdut.admission.entity.Stu;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AdmissionStuDto extends Stu {
    // 录取学院
    String collegeName;
    // 录取专业
    String professionName;
    // 录取志愿编号
    String professionNum;
}
