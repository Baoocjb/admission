package com.gdut.admission.controller;


import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Plan;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.service.IPlanService;
import com.gdut.admission.service.IStuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Bao
 * @since 2022-12-05
 */
@RestController
@RequestMapping("plan")
public class PlanController {
    @Autowired
    private IPlanService planService;

    /**
     * 招生计划文件上传
     */
    @ApiOperation(value = "招生计划文件上传")
    @PostMapping("upload")
    public Result upload(@RequestBody MultipartFile file){
        return planService.upload(file);
    }

    /**
     * 分页显示志愿信息, 按照计划数排序
     * @param currentPage
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "分页显示志愿信息, 按照计划数排序")
    @GetMapping("index")
    public Result index(int currentPage, int pageSize){
        return planService.index(currentPage, pageSize);
    }

    /**
     * 修改招生计划
     */
    @ApiOperation(value = "修改招生计划")
    @PostMapping("update")
    public Result update(@RequestBody Plan plan) throws IOException {
        // 修改招生计划
        return planService.updatePlan(plan);
    }

    /**
     * 删除招生计划
     */
    @ApiOperation(value = "删除招生计划")
    @PostMapping("delete")
    public Result deletePlan(Integer planId){
        // 删除招生计划
        return planService.deletePlan(planId);
    }

    /**
     * 根据招生计划id查询招生计划
     * @param id
     * @return
     */
    @ApiOperation(value = "根据招生计划id查询招生计划")
    @GetMapping("getOne")
    public Result getPlanById(Integer id) {
        Plan plan = planService.getById(id);
        if (id == null || plan == null) {
            return Result.fail("待查询招生计划不存在!");
        }
        return Result.ok(plan);
    }

    /**
     * 新增招生计划
     * @param plan
     * @return
     */
    @ApiOperation(value = "新增招生计划")
    @PostMapping("add")
    public Result addPlan(@RequestBody Plan plan){
        return planService.addPlan(plan);
    }
}
