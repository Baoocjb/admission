package com.gdut.admission.service;

import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Admission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Bao
 * @since 2022-12-06
 */
public interface IAdmissionService extends IService<Admission> {

    Result admission();

    Result professionIndex(int currentPage, int pageSize);
}
