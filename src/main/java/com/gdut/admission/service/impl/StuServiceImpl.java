package com.gdut.admission.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.listener.StuListener;
import com.gdut.admission.mapper.StuMapper;
import com.gdut.admission.service.IStuService;
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
public class StuServiceImpl extends ServiceImpl<StuMapper, Stu> implements IStuService {

    /**
     * 导入学生志愿信息至数据库中
     * @param file
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Result upload(MultipartFile file) {
        if(file == null || file.isEmpty()){
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
            ReadSheet readSheet = EasyExcel.readSheet(1).build();
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
     * @param currentPage
     * @param pageSize
     * @return
     */
    public Result index(int currentPage, int pageSize) {
        Page<Stu> stuPage = new Page<>(currentPage, pageSize);
        LambdaUpdateWrapper<Stu> stuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        stuLambdaUpdateWrapper.orderByDesc(Stu::getScore);
        page(stuPage, stuLambdaUpdateWrapper);
        stuPage.setPages(currentPage);
        return Result.ok(stuPage);
    }
}
