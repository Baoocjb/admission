package com.gdut.admission.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdut.admission.dto.AdmissionStuDto;
import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Admission;
import com.gdut.admission.entity.Plan;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.listener.StuListener;
import com.gdut.admission.mapper.AdmissionMapper;
import com.gdut.admission.mapper.PlanMapper;
import com.gdut.admission.mapper.StuMapper;
import com.gdut.admission.service.IAdmissionService;
import com.gdut.admission.service.IPlanService;
import com.gdut.admission.service.IStuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Bao
 * @since 2022-12-05
 */
@Service
public class StuServiceImpl extends ServiceImpl<StuMapper, Stu> implements IStuService {

    @Autowired
    private StuMapper stuMapper;
    @Autowired
    private AdmissionMapper admissionMapper;
    @Autowired
    private PlanMapper planMapper;

    /**
     * 导入学生志愿信息至数据库中
     *
     * @param file
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Result upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空!");
        }
        // 删除所有的数据再进行导入
        remove(new QueryWrapper<Stu>());
        ExcelReader excelReader = null;
        try {
            excelReader = EasyExcel.read(
                    file.getInputStream(),
                    Stu.class,
                    new StuListener(this)
            ).build();
            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            excelReader.read(readSheet);
            excelReader.finish();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("志愿信息文件导入失败,数据不符合格式!");
        }
        return Result.ok();
    }

    /**
     * 显示所有学生志愿信息,并按照分数排序
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    public Result index(int currentPage, int pageSize) {
        Page<Stu> stuPage = new Page<>(currentPage, pageSize);
        LambdaUpdateWrapper<Stu> stuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        stuLambdaUpdateWrapper.orderByDesc(Stu::getScore);
        page(stuPage, stuLambdaUpdateWrapper);
        return Result.ok(stuPage);
    }

    @Override
    public Result updateStu(Stu stu) {
        if (!assertStuParams(stu)) {
            return Result.fail("参数不能为空!");
        }
        if (getById(stu.getId()) == null) {
            return Result.fail("待删除记录不存在");
        }
        this.updateById(stu);
        return Result.ok();
    }

    /**
     * 校验学生参数
     * @return
     */
    private boolean assertStuParams(Stu stu){
        if (stu == null
                || stu.getName() == null
                || stu.getScore() == null
                || stu.getAdOne() == null
                || stu.getLanguage() == null
                || stu.getIsSwap() == null
                || stu.getStuRank() == null
                || stu.getId() == null
        ) {
            return false;
        }
        return true;
    }

    @Override
    public Result deleteStu(Integer stuId) {
        if (stuId == null || getById(stuId) == null) {
            return Result.fail("待删除记录不存在!");
        }
        removeById(stuId);
        return Result.ok();
    }

    /**
     * 返回退档学生信息
     *
     * @return
     */
    @Override
    public List<Stu> backData() {
        return list(new LambdaUpdateWrapper<Stu>().eq(Stu::getStatus, 2));
    }

    /**
     * 获取条件查询的dto
     * @param admissionStuDto
     * @return
     */
    @Override
    public List<AdmissionStuDto> getAdStuByParams(AdmissionStuDto admissionStuDto, int currentPage, int pageSize) {
        List<Stu> stuList = stuMapper.getStuByParams(admissionStuDto, (currentPage - 1) * pageSize, pageSize);
        List<AdmissionStuDto> admissionStuDtoList = new ArrayList<>(stuList.size());
        for (Stu stu : stuList) {
            // 把退档的学生剔除
            if(stu.getStatus() != 2){
                AdmissionStuDto adDto = new AdmissionStuDto();
                BeanUtils.copyProperties(stu, adDto);
                // 查询该学生对应的录取结果
                Admission admission = admissionMapper.selectOne(new LambdaUpdateWrapper<Admission>().eq(Admission::getStuId, stu.getId()));
                Plan plan = planMapper.selectById(admission.getPlanId());
                adDto.setProfessionNum(plan.getProfessionNum());
                adDto.setProfessionName(plan.getProfessionName());
                adDto.setCollegeName(plan.getCollegeName());
                admissionStuDtoList.add(adDto);
            }
        }
        return admissionStuDtoList;
    }

    @Override
    @Transactional
    public Result addStu(Stu stu) {
        if(!assertStuParams(stu)){
            return Result.fail("添加学生参数不能为空!");
        }
        stuMapper.insert(stu);
        return Result.ok();
    }
}
