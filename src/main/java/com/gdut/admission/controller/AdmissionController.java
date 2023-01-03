package com.gdut.admission.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.gdut.admission.dto.AdmissionStuDto;
import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.service.IAdmissionService;
import com.gdut.admission.service.IStuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

@RestController
@RequestMapping("/admission")
public class AdmissionController {

    @Autowired
    private IAdmissionService admissionService;
    @Autowired
    private IStuService stuService;

    /**
     * 开始录取
     *
     * @return
     */
    @ApiOperation(value = "开始录取")
    @GetMapping("startAd")
    public Result admission() {
        return admissionService.admission();
    }

    /**
     * 统计专业信息
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "统计专业信息")
    @GetMapping("profession")
    public Result professionIndex(int currentPage, int pageSize) {
        return admissionService.professionIndex(currentPage, pageSize);
    }

    /**
     * 统计学院信息
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "统计学院信息")
    @GetMapping("college")
    public Result collegeIndex(int currentPage, int pageSize) {
        return admissionService.collegeIndex(currentPage, pageSize);
    }

    /**
     * 统计学校信息
     *
     * @return
     */
    @ApiOperation(value = "统计学校信息")
    @GetMapping("school")
    public Result schoolIndex() {
        return admissionService.schoolIndex();
    }

    /**
     * 统计退档学生信息
     *
     * @return
     */
    @ApiOperation(value = "统计退档学生信息")
    @GetMapping("back")
    public Result backIndex(int currentPage, int pageSize) {
        return admissionService.backIndex(currentPage, pageSize);
    }

    /**
     * 模糊查询录取结果学生信息
     *
     * @return
     */
    @ApiOperation(value = "模糊查询录取结果学生信息")
    @PostMapping("queryResult")
    public Result getStuAdmissionByParams(@RequestBody(required = false)AdmissionStuDto admissionStuDto, @RequestParam("currentPage") int currentPage,@RequestParam("pageSize") int pageSize) {
        return admissionService.getStuAdmissionByParams(admissionStuDto, currentPage, pageSize);
    }

    /**
     * 查询录取结果前1%学生信息
     *
     * @return
     */
    @ApiOperation(value = "查询录取结果前1%学生信息")
    @GetMapping("queryTheBest")
    public Result queryTheBest(@RequestParam("currentPage") int currentPage,@RequestParam("pageSize") int pageSize) {
        AdmissionStuDto admissionStuDto = new AdmissionStuDto();
        admissionStuDto.setStatus(4);
        return admissionService.getTheBestStus(admissionStuDto, currentPage, pageSize);
    }

    /**
     * 打印退档队列
     * @param response
     * @throws IOException
     */
    @ApiOperation(value = "打印退档队列")
    @GetMapping("downloadBack")
    public void downloadBack(HttpServletResponse response) throws IOException {
        // 忽略字段
        Set<String> excludeColumnFiledNames = new HashSet<>();
        excludeColumnFiledNames.add("status");
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("志愿录取退档学生表", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream(), Stu.class).autoCloseStream(Boolean.FALSE).excludeColumnFiledNames(excludeColumnFiledNames).sheet("退档学生表")
                    .doWrite(stuService.backData());
        } catch (Exception e) {
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = MapUtils.newHashMap();
            map.put("success", "false");
            map.put("errorMsg", "下载文件失败" + e.getMessage());
            map.put("data", null);
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    /**
     * 打印录取结果
     * @param response
     * @throws IOException
     */
    @ApiOperation(value = "打印拟录取学生表")
    @GetMapping("downloadAdmission")
    public void downloadAdmission(HttpServletResponse response) throws IOException {
        // 忽略字段
        Set<String> excludeColumnFiledNames = new HashSet<>();
        excludeColumnFiledNames.add("status");
        excludeColumnFiledNames.add("id");

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("拟录取学生表", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 这里需要设置不关闭流
            AdmissionStuDto admissionStuDto = new AdmissionStuDto();
            admissionStuDto.setStatus(4);
            EasyExcel.write(response.getOutputStream(), AdmissionStuDto.class).autoCloseStream(Boolean.FALSE).excludeColumnFiledNames(excludeColumnFiledNames).sheet("退档学生表")
                    .doWrite(stuService.getAdStuByParams(admissionStuDto, 1, admissionService.count()));
        } catch (Exception e) {
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = MapUtils.newHashMap();
            map.put("success", "false");
            map.put("errorMsg", "下载文件失败" + e.getMessage());
            map.put("data", null);
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    /**
     * 打印前百分之一录取结果
     * @param response
     * @throws IOException
     */
    @ApiOperation(value = "打印前百分之一录取结果")
    @GetMapping("downloadTheBest")
    public void downloadTheBest(HttpServletResponse response) throws IOException {
        // 忽略字段
        Set<String> excludeColumnFiledNames = new HashSet<>();
        excludeColumnFiledNames.add("status");
        excludeColumnFiledNames.add("id");

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("前1%的拟录取学生表", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 这里需要设置不关闭流
            AdmissionStuDto admissionStuDto = new AdmissionStuDto();
            admissionStuDto.setStatus(4);
            EasyExcel.write(response.getOutputStream(), AdmissionStuDto.class).autoCloseStream(Boolean.FALSE).excludeColumnFiledNames(excludeColumnFiledNames).sheet("退档学生表")
                    .doWrite(stuService.getAdStuByParams(admissionStuDto, 1, (int)(admissionService.count() * 0.01)));
        } catch (Exception e) {
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = MapUtils.newHashMap();
            map.put("success", "false");
            map.put("errorMsg", "下载文件失败" + e.getMessage());
            map.put("data", null);
            response.getWriter().println(JSON.toJSONString(map));
        }
    }
}
