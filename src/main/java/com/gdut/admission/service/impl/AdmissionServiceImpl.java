package com.gdut.admission.service.impl;

import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gdut.admission.dto.AdmissionDto;
import com.gdut.admission.dto.Result;
import com.gdut.admission.entity.Admission;
import com.gdut.admission.entity.Plan;
import com.gdut.admission.entity.SortPlan;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.mapper.AdmissionMapper;
import com.gdut.admission.service.IAdmissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdut.admission.service.IPlanService;
import com.gdut.admission.service.IStuService;
import com.gdut.admission.util.InsertConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 * 服务实现类
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
     * 每隔1000条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 1000;

    // 装载录取数据
    private List<Admission> admissionList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    // 装载录取学生
    private List<Stu> admissionStus = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    // 装载退档学生
    private List<Stu> backList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    // 调剂队列
    private Deque<Stu> swapDeque;

    // 录取计划映射
    private Map<Integer, Plan> planMap;

    // 空闲专业队列
    private Map<Integer, Double> freePlan;

    /**
     * 录取学生
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Result admission() {
        // 初始化调剂队列
        swapDeque = new ArrayDeque<>();

        LambdaQueryWrapper<Stu> stuQueryWrapper = new LambdaQueryWrapper<>();
        // 查询所有考生并按照排名排序
        stuQueryWrapper.orderByAsc(Stu::getStuRank);
        List<Stu> stuList = stuService.list(stuQueryWrapper);
        Iterator<Stu> iterator = stuList.iterator();

        // 获取招生计划表, 并放入Map记录
        List<Plan> planList = planService.list();
        planMap = new HashMap<>();
        freePlan = new HashMap<>();
        for (Plan plan : planList) {
            planMap.put(plan.getId(), plan);
            // 初始化空闲专业哈希表, 把所有专业的最低分赋值为0
            freePlan.put(plan.getId(), 0.0);
        }

        // 遍历所有学生
        while (iterator.hasNext()) {
            Stu stu = iterator.next();
            // 录取结果
            boolean adSuccess = false;
            int planId = -1;
            // 遍历该学生志愿信息
            // 第一个志愿
            String adOne = stu.getAdOne();
            Plan plan = planService.getOne(new LambdaUpdateWrapper<Plan>().eq(Plan::getProfessionNum, adOne));
            adSuccess = successAdmission(plan.getId(), stu);
            if (adSuccess) {
                planId = admissionNormal(plan, stu);
            }
            // 第二个志愿
            if (!adSuccess) {
                String adTwo = stu.getAdTwo();
                if(adTwo != null){
                    plan = planService.getOne(new LambdaUpdateWrapper<Plan>().eq(Plan::getProfessionNum, adTwo));
                    adSuccess = successAdmission(plan.getId(), stu);
                }else{
                    plan = null;
                    adSuccess = successAdmission(0, stu);
                }
                if (adSuccess) {
                    planId = admissionNormal(plan, stu);
                }
            }
            // 第三个志愿
            if (!adSuccess) {
                String adThree = stu.getAdThree();
                if(adThree != null){
                    plan = planService.getOne(new LambdaUpdateWrapper<Plan>().eq(Plan::getProfessionNum, adThree));
                    adSuccess = successAdmission(plan.getId(), stu);
                }else{
                    plan = null;
                    adSuccess = successAdmission(0, stu);
                }
                if (adSuccess) {
                    planId = admissionNormal(plan, stu);
                }
            }
            // 第四个志愿
            if (!adSuccess) {
                String adFour = stu.getAdFour();
                if(adFour != null){
                    plan = planService.getOne(new LambdaUpdateWrapper<Plan>().eq(Plan::getProfessionNum, adFour));
                    adSuccess = successAdmission(plan.getId(), stu);
                }else{
                    plan = null;
                    adSuccess = successAdmission(0, stu);
                }
                if (adSuccess) {
                    planId = admissionNormal(plan, stu);
                }
            }
            // 第五个志愿
            if (!adSuccess) {
                String adFive = stu.getAdFive();
                if(adFive != null){
                    plan = planService.getOne(new LambdaUpdateWrapper<Plan>().eq(Plan::getProfessionNum, adFive));
                    adSuccess = successAdmission(plan.getId(), stu);
                }else{
                    plan = null;
                    adSuccess = successAdmission(0, stu);
                }
                if (adSuccess) {
                    planId = admissionNormal(plan, stu);
                }
            }
            // 第六个志愿
            if (!adSuccess) {
                String adSix = stu.getAdSix();
                if(adSix != null){
                    plan = planService.getOne(new LambdaUpdateWrapper<Plan>().eq(Plan::getProfessionNum, adSix));
                    adSuccess = successAdmission(plan.getId(), stu);
                }else{
                    plan = null;
                    adSuccess = successAdmission(0, stu);
                }
                if (adSuccess) {
                    planId = admissionNormal(plan, stu);
                }
            }
            // 所有志愿都不能被录取
            if (!adSuccess) {
                // 查看该学生是否允许调剂
                if (Integer.valueOf(1).equals(stu.getIsSwap())) {
                    // 加入调剂队列
                    // 此时的学生在队列中成绩由高到低排名
                    swapDeque.offer(stu);
                } else {
                    // 加入退档队列, 即把学生状态改为退档
                    stu.setStatus(2);
                    backList.add(stu);
                    if (backList.size() >= BATCH_COUNT) {
                        // 批量更新
                        stuService.updateBatchById(backList);
                        backList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                    }
                }
            } else {
                // 录取, 将学生插入录取队列中, 以方便批量更新
                stu.setStatus(1);
                admissionStus.add(stu);
                if (admissionStus.size() >= BATCH_COUNT) {
                    // 批量更新
                    stuService.updateBatchById(admissionStus);
                    admissionStus = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }

                // 将学生插入录取表中
                Admission admission = new Admission();
                admission.setPlanId(planId);
                admission.setStuId(stu.getId());
                admissionList.add(admission);
                if (admissionList.size() >= BATCH_COUNT) {
                    // 批量插入
                    InsertConsumer.insertData(admissionList, this::saveBatch);
                    // 存储完成清理 list
                    admissionList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }
            }
            // 移除学生
            iterator.remove();
        }

        // 未完成招生计划的专业按该专业已经录取的最后一名考生的总分排序
        List<SortPlan> sortPlanList = new LinkedList<>();
        for (Map.Entry<Integer, Plan> planEntry : planMap.entrySet()) {
            SortPlan sortPlan = new SortPlan();
            Integer planId = planEntry.getKey();
            sortPlan.setPlanId(planId);
            sortPlan.setMinScore(freePlan.get(planId));
            sortPlanList.add(sortPlan);
        }

        Comparator<SortPlan> comparator = new Comparator<SortPlan>() {
            @Override
            public int compare(SortPlan o1, SortPlan o2) {
                return (int) (o2.getMinScore() - o1.getMinScore());
            }
        };
        sortPlanList.sort(comparator);
        // 调剂录取
        swapAdmission(sortPlanList, swapDeque);

        stuService.updateBatchById(backList);
        stuService.updateBatchById(admissionStus);
        InsertConsumer.insertData(admissionList, this::saveBatch);
        return Result.ok();
    }

    /**
     * 调剂录取
     *
     * @param swapDeque
     */
    private void swapAdmission(List<SortPlan> sortPlanList, Deque<Stu> swapDeque) {
        while (!swapDeque.isEmpty()) {
            // 得到调剂录取最高成绩的学生
            Stu stu = swapDeque.peek();

            boolean isSuccess = false;
            for (SortPlan sortPlan : sortPlanList) {
                // 最高分数的调剂专业,判断是否能录取
                isSuccess = successAdmission(sortPlan.getPlanId(), stu);
                if(isSuccess){
                    // 该学生弹出调剂录取队列, 加入录取队列
                    swapDeque.pop();
                    stu.setStatus(3);
                    admissionStus.add(stu);
                    if (admissionStus.size() >= BATCH_COUNT) {
                        // 批量更新
                        stuService.updateBatchById(admissionStus);
                        admissionStus = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                    }
                    // 将学生插入录取表中
                    Admission admission = new Admission();
                    admission.setPlanId(sortPlan.getPlanId());
                    admission.setStuId(stu.getId());
                    admissionList.add(admission);
                    if (admissionList.size() >= BATCH_COUNT) {
                        // 批量插入
                        InsertConsumer.insertData(admissionList, this::saveBatch);
                        // 存储完成清理 list
                        admissionList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                    }
                    swapAdmission(sortPlan.getPlanId());
                    break;
                }
            }

            // 不能被录取, 放入退档队列
            if(!isSuccess){
                // 加入退档队列, 即把学生状态改为退档
                stu.setStatus(2);
                backList.add(stu);
                if (backList.size() >= BATCH_COUNT) {
                    // 批量更新
                    stuService.updateBatchById(backList);
                    backList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }
            }
        }
    }

    /**
     * 调剂录取
     *
     * @param planId
     * @return
     */
    private void swapAdmission(Integer planId) {
        Plan plan = planMap.get(planId);
        plan.setPlanNum(plan.getPlanNum() - 1);
        planMap.put(planId, plan);
        if(plan.getPlanNum() == 0){
            planMap.remove(planId);
        }
    }

    /**
     * 判断是否能够录取
     *
     * @param planId
     * @param stu
     * @return -1代表录取失败, 返回专业id
     */
    private boolean successAdmission(Integer planId, Stu stu) {
        // 判断招生计划数
        Plan plan = planMap.get(planId);
        if (plan == null || plan.getPlanNum() == 0) {
            return false;
        }
        // 判断是否能被录取
        // 判断语种
        String language = plan.getLanguage();
        if (language != null && !language.equals(stu.getLanguage())) {
            return false;
        }
        // 判断体检受限
        if (stu.getBodyTest() != null) {
            String bodyTest = stu.getBodyTest();
            String[] bodyTests = bodyTest.split(",");
            // 体检受限1
            if (plan.getTestLimit1() != null) {
                for (String stuBody : bodyTests) {
                    if (plan.getTestLimit1().equals(stuBody)) {
                        return false;
                    }
                }
            }
            // 体检受限2
            if (plan.getTestLimit2() != null) {
                for (String stuBody : bodyTests) {
                    if (plan.getTestLimit2().equals(stuBody)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 正常录取
     */
    private int admissionNormal(Plan plan, Stu stu) {
        plan.setPlanNum(plan.getPlanNum() - 1);
        planMap.put(plan.getId(), plan);
        if (plan.getPlanNum() == 0) {
            // 移除招生计划, 剩余的用于调剂
            planMap.remove(plan.getId());
        }
        // 记录录取的最后一名考生的总分
        freePlan.put(plan.getId(), stu.getScore());
        return plan.getId();
    }
}
