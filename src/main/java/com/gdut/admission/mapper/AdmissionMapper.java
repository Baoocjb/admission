package com.gdut.admission.mapper;

import com.gdut.admission.dto.AdmissionDto;
import com.gdut.admission.entity.Admission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Bao
 * @since 2022-12-06
 */
public interface AdmissionMapper extends BaseMapper<Admission> {

    List<Admission> queryProfessionMaxRank();

    List<Admission> queryProfessionMinRank();
}
