package com.gdut.admission.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Plan;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.listener.PlanListener;
import com.gdut.admission.listener.StuListener;
import com.gdut.admission.mapper.PlanMapper;
import com.gdut.admission.service.IPlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Bao
 * @since 2022-12-05
 */
@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements IPlanService {

    @Autowired
    private PlanMapper planMapper;

    /**
     * 招生计划信息导入至数据库中
     *
     * @param file
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Result upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不正确!");
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if(!"xlsx".equals(suffix) && !"xls".equals(suffix)){
            return Result.fail("招生计划文件导入失败,文件不符合格式!");
        }
        // 删除所有的数据再进行导入
        remove(new QueryWrapper<Plan>());
        ExcelReader excelReader = null;
        try {
            excelReader = EasyExcel.read(
                    file.getInputStream(),
                    Plan.class,
                    new PlanListener(this)
            ).build();
            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            excelReader.read(readSheet);
            excelReader.finish();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("招生计划文件导入失败,数据不符合格式!");
        }
        return Result.ok();
    }

    /**
     * 将招生计划分页展示
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    public Result index(int currentPage, int pageSize) {
        Page<Plan> planPage = new Page<>(currentPage, pageSize);
        LambdaUpdateWrapper<Plan> stuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        stuLambdaUpdateWrapper.orderByAsc(Plan::getProfessionNum);
        page(planPage, stuLambdaUpdateWrapper);
        return Result.ok(planPage);
    }

    @Override
    public Result updatePlan(Plan plan) {
        if (!assertPlanParams(plan)) {
            return Result.fail("参数不能为空!");
        }
        updateById(plan);
        return Result.ok();
    }

    /**
     * 校验计划参数
     * @param plan
     * @return
     */
    private boolean assertPlanParams(Plan plan){
        if (plan == null
                || plan.getId() == null
                || plan.getGroupId() == null
                || plan.getProfessionNum() == null
                || plan.getProfessionName() == null
                || plan.getCollegeName() == null
                || plan.getPlanNum() == null
                || plan.getLocation() == null
        ) {
            return false;
        }
        return true;
    }

    @Override
    public Result deletePlan(Integer planId) {
        if (planId == null || getById(planId) == null){
            return Result.fail("待删除记录不存在!");
        }
        removeById(planId);
        return Result.ok();
    }

    @Override
    public Result addPlan(Plan plan) {
        if (!assertPlanParams(plan)) {
            return Result.fail("添加计划参数不能为空!");
        }
        planMapper.insert(plan);
        return Result.ok();
    }
}
