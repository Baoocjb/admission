package com.gdut.admission.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.listener.StuListener;
import com.gdut.admission.service.IStuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Bao
 * @since 2022-12-05
 */
@RestController
@RequestMapping("stu")
public class StuController {
    @Autowired
    private IStuService stuService;

    /**
     * 志愿文件上传
     */
    @PostMapping("upload")
    public Result upload(@RequestBody MultipartFile file) {
        return stuService.upload(file);
    }

    /**
     * 分页显示志愿信息,并按照分数排序
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @GetMapping("index")
    public Result index(int currentPage, int pageSize) {
        // TODO 模糊查询
        return stuService.index(currentPage, pageSize);
    }

    /**
     * 修改学生志愿信息
     */
    @PostMapping("update")
    public Result updateStu(@RequestBody Stu stu) {
        return stuService.updateStu(stu);
    }

    /**
     * 删除学生志愿信息
     */
    @PostMapping("delete")
    public Result delete(Integer stuId) {
        return stuService.deleteStu(stuId);
    }

    /**
     * 根据学生id查询学生
     * @param id
     * @return
     */
    @GetMapping("getOne")
    public Result getStuById(Integer id) {
        Stu stu = stuService.getById(id);
        if (id == null || stu == null) {
            return Result.fail("待查询学生不存在!");
        }
        return Result.ok(stu);
    }

    @PostMapping("add")
    public Result addStu(@RequestBody Stu stu){
        return stuService.addStu(stu);
    }
}
