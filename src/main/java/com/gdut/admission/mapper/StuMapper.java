package com.gdut.admission.mapper;

import com.gdut.admission.dto.AdmissionStuDto;
import com.gdut.admission.entity.Stu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Bao
 * @since 2022-12-05
 */
public interface StuMapper extends BaseMapper<Stu> {

    List<Stu> getStuByParams(AdmissionStuDto admissionStuDto);
}
