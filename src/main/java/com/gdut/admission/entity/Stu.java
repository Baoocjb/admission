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
@TableName("t_stu")
public class Stu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 考生号
     */
    @ExcelProperty(index = 0)
    private String stuNum;

    /**
     * 学生姓名
     */
    @ExcelProperty(index = 1)
    private String name;

    /**
     * 专业组号
     */
    @ExcelProperty(index = 2)
    private Integer groupId;

    /**
     * 投档成绩
     */
    @ExcelProperty(index = 3)
    private Double score;

    /**
     * 服从调剂
     */
    @ExcelProperty(index = 4)
    private String isSwap;

    /**
     * 专业志愿1
     */
    @ExcelProperty(index = 5)
    private String adOne;

    /**
     * 专业志愿2
     */
    @ExcelProperty(index = 6)
    private String adTwo;

    /**
     * 专业志愿3
     */
    @ExcelProperty(index = 7)
    private String adThree;

    /**
     * 专业志愿4
     */
    @ExcelProperty(index = 8)
    private String adFour;

    /**
     * 专业志愿5
     */
    @ExcelProperty(index = 9)
    private String adFive;

    /**
     * 专业志愿6
     */
    @ExcelProperty(index = 10)
    private String adSix;

    /**
     * 外语语种
     */
    @ExcelProperty(index = 11)
    private String language;

    /**
     * 体检备注
     */
    @ExcelProperty(index = 12)
    private String bodyTest;

    /**
     * 录取状态: 0 未录取, 1 已录取, 2 退档
     */
    private Integer status;
}
