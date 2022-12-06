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
    @ExcelProperty("专业组号")
    private Integer groupId;

    /**
     * 专业代号
     */
    @ExcelProperty("专业代号")
    private String professionNum;

    /**
     * 专业名称
     */
    @ExcelProperty("专业名称")
    private String professionName;

    /**
     * 学院名称
     */
    @ExcelProperty("学院名称")
    private String collegeName;

    /**
     * 招生计划
     */
    @ExcelProperty("招生计划数")
    private Integer planNum;

    /**
     * 专业备注
     */
    @ExcelProperty("专业备注")
    private String comment;

    /**
     * 办学地点
     */
    @ExcelProperty("办学地点")
    private String location;

    /**
     * 语种限制
     */
    @ExcelProperty("外语语种")
    private String language;

    /**
     * 体检限制1
     */
    @ExcelProperty("体检受限1")
    private String testLimit1;

    /**
     * 体检限制2
     */
    @ExcelProperty("体检受限2")
    private String testLimit2;


}
