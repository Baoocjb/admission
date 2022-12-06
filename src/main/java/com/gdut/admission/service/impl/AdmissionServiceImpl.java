package com.gdut.admission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Admission;
import com.gdut.admission.entity.Plan;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.mapper.AdmissionMapper;
import com.gdut.admission.service.IAdmissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdut.admission.service.IPlanService;
import com.gdut.admission.service.IStuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Bao
 * @since 2022-12-06
 */
@Service
public class AdmissionServiceImpl extends ServiceImpl<AdmissionMapper, Admission> implements IAdmissionService {

    @Autowired
    private IStuService stuService;
    @Autowired
    private IPlanService planService;


    /**
     * 录取学生
     * @return
     */
    @Override
    public Result admission() {
        // 待录取考生队列
        LambdaQueryWrapper<Stu> stuQueryWrapper = new LambdaQueryWrapper<>();
        // 查询未录取的考生并按照排名排序
        stuQueryWrapper.eq(Stu::getStatus, 0);
        stuQueryWrapper.orderByDesc(Stu::getStuRank);
        List<Stu> stuList = stuService.list(stuQueryWrapper);
        Iterator<Stu> iterator = stuList.iterator();

        // 获取招生计划表
        List<Plan> planList = planService.list();
        // 遍历所有学生
        while (iterator.hasNext()){
            Stu stu = iterator.next();
            // 遍历该学生志愿信息
            String adOne = stu.getAdOne();



            iterator.remove();
        }



        return null;
    }
}
