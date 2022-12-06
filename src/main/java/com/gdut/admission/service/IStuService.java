package com.gdut.admission.service;

import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Stu;
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
public interface IStuService extends IService<Stu> {

    Result upload(MultipartFile file);

    Result index(int currentPage, int pageSize);

    Result update(Stu stu);

    Result deleteStu(Integer stuId);
}
