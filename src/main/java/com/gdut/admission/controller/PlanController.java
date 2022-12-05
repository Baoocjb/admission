package com.gdut.admission.controller;


import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Plan;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.service.IPlanService;
import com.gdut.admission.service.IStuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Bao
 * @since 2022-12-05
 */
@RestController
@RequestMapping("/plan")
public class PlanController {
    @Autowired
    private IPlanService planService;

    /**
     * 招生计划文件上传
     */
    @PostMapping("upload")
    public Result upload(@RequestBody MultipartFile file){
        return planService.upload(file);
    }

    /**
     * 分页显示志愿信息,并按照分数排序
     * @param currentPage
     * @param pageSize
     * @return
     */
    @GetMapping("index")
    public Result index(int currentPage, int pageSize){
        return planService.index(currentPage, pageSize);
    }

    /**
     * 修改招生计划
     */
    @PostMapping("update")
    public Result update(@RequestBody Plan plan){
        return null;
    }

    /**
     * 删除招生计划
     */
    @PostMapping("delete")
    public Result delete(Integer planId){
        return null;
    }
}
