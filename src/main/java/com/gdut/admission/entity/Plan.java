package com.gdut.admission.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author Bao
 * @since 2022-12-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_plan")
public class Plan implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 专业组号	
     */
    @ExcelProperty(index = 0)
    private Integer groupId;

    /**
     * 专业代号
     */
    @ExcelProperty(index = 1)
    private String professionId;

    /**
     * 专业名称
     */
    @ExcelProperty(index = 2)
    private String professionName;

    /**
     * 学院名称
     */
    @ExcelProperty(index = 3)
    private String collegeName;

    /**
     * 招生计划
     */
    @ExcelProperty(index = 4)
    private Integer planNum;

    /**
     * 专业备注
     */
    @ExcelProperty(index = 5)
    private String comment;

    /**
     * 办学地点
     */
    @ExcelProperty(index = 6)
    private String location;

    /**
     * 语种限制
     */
    @ExcelProperty(index = 7)
    private String language;

    /**
     * 体检限制1
     */
    @ExcelProperty(index = 8)
    private String testLimit1;

    /**
     * 体检限制2
     */
    @ExcelProperty(index = 9)
    private String testLimit2;


}
