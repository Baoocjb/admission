package com.gdut.admission.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.gdut.admission.entity.Stu;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AdmissionStuDto extends Stu {
    // 录取学院
    @ExcelProperty("录取学院")
    String collegeName;
    // 录取专业
    @ExcelProperty("录取专业")
    String professionName;
    // 录取志愿编号
    @ExcelProperty("录取志愿编号")
    String professionNum;
}
