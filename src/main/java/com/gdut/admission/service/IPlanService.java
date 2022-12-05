package com.gdut.admission.service;

import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Plan;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Bao
 * @since 2022-12-05
 */
public interface IPlanService extends IService<Plan> {

    Result upload(MultipartFile file);

    Result index(int currentPage, int pageSize);
}
