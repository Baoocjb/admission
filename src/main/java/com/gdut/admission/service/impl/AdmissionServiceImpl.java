package com.gdut.admission.service.impl;

import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdut.admission.dto.ProfessionDto;
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
    @Autowired
    private AdmissionMapper admissionMapper;

    /**
     * 每隔1000条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 1000;

    // 装载录取数据
    private List<Admission> admissionList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    // 装载录取学生
    private List<Stu> updateAdmissionStus = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

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
        // 删除所有的数据再进行导入
        remove(new QueryWrapper<Admission>());
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
                plan = planMap.get(plan.getId());
                planId = normalAdmission(plan, stu);
            }
            // 第二个志愿
            if (!adSuccess) {
                String adTwo = stu.getAdTwo();
                if(adTwo != null){
                    plan = planService.getOne(new LambdaUpdateWrapper<Plan>().eq(Plan::getProfessionNum, adTwo));
                    adSuccess = successAdmission(plan.getId(), stu);
                }else{
                    plan = null;
                }
                if (adSuccess) {
                    plan = planMap.get(plan.getId());
                    planId = normalAdmission(plan, stu);
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
                }
                if (adSuccess) {
                    plan = planMap.get(plan.getId());
                    planId = normalAdmission(plan, stu);
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
                }
                if (adSuccess) {
                    plan = planMap.get(plan.getId());
                    planId = normalAdmission(plan, stu);
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
                }
                if (adSuccess) {
                    plan = planMap.get(plan.getId());
                    planId = normalAdmission(plan, stu);
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
                    plan = planMap.get(plan.getId());
                    planId = normalAdmission(plan, stu);
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
                    saveBackStus(stu);
                }
            } else {
                // 保存录取的学生
                saveAdmission(1, stu, planId);
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

        stuService.updateBatchById(updateAdmissionStus);
        updateAdmissionStus = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

        stuService.updateBatchById(backList);
        backList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

        saveBatch(admissionList);
        admissionList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        // 调剂录取
        swapAdmission(sortPlanList, swapDeque);

        stuService.updateBatchById(backList);
        stuService.updateBatchById(updateAdmissionStus);
        saveBatch(admissionList);
        return Result.ok();
    }

    /**
     * 保存退档的学生
     */
    private void saveBackStus(Stu stu){
        // 加入退档队列, 即把学生状态改为退档
        stu.setStatus(2);
        backList.add(stu);
        if (backList.size() >= BATCH_COUNT) {
            // 批量更新
            stuService.updateBatchById(backList);
            backList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 保存录取的学生
     * @param status
     * @param stu
     * @param planId
     */
    private void saveAdmission(Integer status,Stu stu, Integer planId){
        // 录取, 将学生插入录取队列中, 以方便批量更新
        stu.setStatus(status);
        updateAdmissionStus.add(stu);
        if (updateAdmissionStus.size() >= BATCH_COUNT) {
            // 批量更新
            stuService.updateBatchById(updateAdmissionStus);
            updateAdmissionStus = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }

        // 将学生插入录取表中
        Admission admission = new Admission();
        admission.setPlanId(planId);
        admission.setStuId(stu.getId());
        admissionList.add(admission);
        if (admissionList.size() >= BATCH_COUNT) {
            // 批量插入
            saveBatch(admissionList);
            // 存储完成清理 list
            admissionList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
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
                    saveAdmission(3, stu, sortPlan.getPlanId());
                    swapAdmission(sortPlan.getPlanId());
                    break;
                }
            }
            // 不能被录取, 放入退档队列
            if(!isSuccess){
                saveBackStus(stu);
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
        if(plan.getPlanNum() <= 0){
            planMap.remove(planId);
            return;
        }
        planMap.put(planId, plan);
    }

    /**
     * 正常录取
     */
    private int normalAdmission(Plan plan, Stu stu) {
        plan.setPlanNum(plan.getPlanNum() - 1);
        // 记录录取的最后一名考生的总分
        freePlan.put(plan.getId(), stu.getScore());
        if (plan.getPlanNum() <= 0) {
            // 移除招生计划, 剩余的用于调剂
            planMap.remove(plan.getId());
            return plan.getId();
        }
        planMap.put(plan.getId(), plan);
        return plan.getId();
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
        if (plan == null || plan.getPlanNum() <= 0) {
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


    @Override
    public Result professionIndex(int currentPage, int pageSize) {
        // 查询每个专业的最高分, 每个专业的最低分, 每个专业的平均分,
        Map<Integer , ProfessionDto> professionDtoMap = new HashMap<>();
        List<Plan> planList = planService.list();
        // 填充进map
        for (Plan plan : planList) {
            ProfessionDto professionDto = new ProfessionDto();
            professionDto.setProfessionNum(plan.getProfessionNum());
            professionDto.setProfessionName(plan.getProfessionName());
            professionDtoMap.put(plan.getId(), professionDto);
        }
        // 填充最高分和最高排位
        List<Admission> maxRankList = admissionMapper.queryProfessionMaxRank();
        for (Admission admission : maxRankList) {
            Stu stu = stuService.getById(admission.getStuId());
            ProfessionDto professionDto = professionDtoMap.get(admission.getPlanId());
            professionDto.setMaxScore(stu.getScore());
            professionDto.setMaxRank(stu.getStuRank());
            professionDtoMap.put(admission.getPlanId(), professionDto);
        }
        // 填充最低分和最低排位
        List<Admission> minRankList = admissionMapper.queryProfessionMinRank();
        for (Admission admission : minRankList) {
            Stu stu = stuService.getById(admission.getStuId());
            ProfessionDto professionDto = professionDtoMap.get(admission.getPlanId());
            professionDto.setMinScore(stu.getScore());
            professionDto.setMinRank(stu.getStuRank());
            professionDtoMap.put(admission.getPlanId(), professionDto);
        }

        // 分页
        Collection<ProfessionDto> records = professionDtoMap.values();
        List<ProfessionDto> professionDtos = new ArrayList<>(records);
        Page<ProfessionDto> professionDtoPage = new Page<>(currentPage, pageSize);
        professionDtoPage.setRecords(professionDtos);
        return Result.ok();
    }
}
