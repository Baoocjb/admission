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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Bao
 * @since 2022-12-05
 */
@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements IPlanService {

    /**
     * 招生计划信息导入至数据库中
     * @param file
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Result upload(MultipartFile file) {
        if(file == null || file.isEmpty()){
            return Result.fail("文件不能为空!");
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
     * @param currentPage
     * @param pageSize
     * @return
     */
    public Result index(int currentPage, int pageSize) {
        Page<Plan> planPage = new Page<>(currentPage, pageSize);
        LambdaUpdateWrapper<Plan> stuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        stuLambdaUpdateWrapper.orderByAsc(Plan::getProfessionNum);
        page(planPage, stuLambdaUpdateWrapper);
        planPage.setPages(currentPage);
        return Result.ok(planPage);
    }
}
