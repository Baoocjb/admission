package com.gdut.admission.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.service.IAdmissionService;
import com.gdut.admission.service.IStuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    @GetMapping("college")
    public Result collegeIndex(int currentPage, int pageSize) {
        return admissionService.collegeIndex(currentPage, pageSize);
    }

    /**
     * 统计学校信息
     *
     * @return
     */
    @GetMapping("school")
    public Result schoolIndex() {
        return admissionService.schoolIndex();
    }

    /**
     * 统计退档学生信息
     *
     * @return
     */
    @GetMapping("back")
    public Result backIndex(int currentPage, int pageSize) {
        return admissionService.backIndex(currentPage, pageSize);
    }

    @GetMapping("downloadBack")
    public void downloadBack(HttpServletResponse response) throws IOException {
        // 忽略字段
        Set<String> excludeColumnFiledNames = new HashSet<String>();
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
}
