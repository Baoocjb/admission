package com.gdut.admission.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class AdmissionStusQuery {
    String collegeName;
    String stuName;
    String proName;
    int state;

}
