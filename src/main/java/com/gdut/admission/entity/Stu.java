package com.gdut.admission.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.bind.DefaultValue;

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
@TableName("t_stu")
public class Stu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生姓名
     */
    @ExcelProperty("姓名")
    private String name;

    /**
     * 投档成绩
     */
    @ExcelProperty("总分")
    private Double score;

    /**
     * 专业志愿1
     */
    @ExcelProperty("志愿1")
    private String adOne;

    /**
     * 专业志愿2
     */
    @ExcelProperty("志愿2")
    private String adTwo;

    /**
     * 专业志愿3
     */
    @ExcelProperty("志愿3")
    private String adThree;

    /**
     * 专业志愿4
     */
    @ExcelProperty("志愿4")
    private String adFour;

    /**
     * 专业志愿5
     */
    @ExcelProperty("志愿5")
    private String adFive;

    /**
     * 专业志愿6
     */
    @ExcelProperty("志愿6")
    private String adSix;

    /**
     * 服从调剂
     */
    @ExcelProperty("调剂")
    private Integer isSwap;

    /**
     * 学生排位
     */
    @ExcelProperty("排位")
    private Integer stuRank;

    /**
     * 外语语种
     */
    @ExcelProperty("外语语种")
    private String language;

    /**
     * 体检备注
     */
    @ExcelProperty("体检筛选备注")
    private String bodyTest;

    /**
     * 录取状态: 0 未录取, 1 已录取, 2 退档
     */
    private Integer status;


}
