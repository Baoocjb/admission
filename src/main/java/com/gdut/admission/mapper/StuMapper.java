package com.gdut.admission.mapper;

import com.gdut.admission.dto.AdmissionStuDto;
import com.gdut.admission.entity.Stu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

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

    List<Stu> getStuByParams(@Param("admissionStuDto") AdmissionStuDto admissionStuDto, @Param("startIndex") int startIndex, @Param("pageSize") int pageSize);

    int getStuCountByParams(@Param("admissionStuDto")AdmissionStuDto admissionStuDto);

}
