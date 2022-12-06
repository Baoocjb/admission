package com.gdut.admission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author Bao
 * @since 2022-12-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_admission")
public class Admission implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 学生id
     */
    private Integer stuId;

    /**
     * 专业代号
     */
    private String professionId;


}
