package com.gdut.admission.service.impl;

import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdut.admission.dto.*;
import com.gdut.admission.entity.Admission;
import com.gdut.admission.entity.Plan;
import com.gdut.admission.entity.SortPlan;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.mapper.AdmissionMapper;
import com.gdut.admission.service.IAdmissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdut.admission.service.IPlanService;
import com.gdut.admission.service.IStuService;
import org.springframework.beans.BeanUtils;
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
     * 每隔3000条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 3000;

    // 装载录取数据
    private List<Admission> admissionList;

    // 装载录取学生
    private List<Stu> admittedStusList;

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
        // 初始化列表
        admissionList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        admittedStusList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        // 删除所有的数据再进行导入
        this.remove(new QueryWrapper<Admission>());
        // 初始化调剂队列
        swapDeque = new ArrayDeque<>();

        // 查询所有考生并按照排名排序
        LambdaQueryWrapper<Stu> stuQueryWrapper = new LambdaQueryWrapper<>();
        stuQueryWrapper.orderByAsc(Stu::getStuRank);
        List<Stu> stuList = stuService.list(stuQueryWrapper);

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
        Iterator<Stu> iterator = stuList.iterator();
        while (iterator.hasNext()) {
            Stu stu = iterator.next();
            // 录取结果
            boolean adSuccess = false;
            int planId = -1;
            // 遍历该学生志愿信息
            // 第一个志愿
            if (!adSuccess) {
                String adOne = stu.getAdOne();
                planId = tryAdmissionAndUpdateAdStatus(adOne, stu);
                adSuccess = planId == -1 ? false : true;
            }
            // 第二个志愿
            if (!adSuccess) {
                String adTwo = stu.getAdTwo();
                planId = tryAdmissionAndUpdateAdStatus(adTwo, stu);
                adSuccess = planId == -1 ? false : true;
            }
            // 第三个志愿
            if (!adSuccess) {
                String adThree = stu.getAdThree();
                planId = tryAdmissionAndUpdateAdStatus(adThree, stu);
                adSuccess = planId == -1 ? false : true;
            }
            // 第四个志愿
            if (!adSuccess) {
                String adFour = stu.getAdFour();
                planId = tryAdmissionAndUpdateAdStatus(adFour, stu);
                adSuccess = planId == -1 ? false : true;
            }
            // 第五个志愿
            if (!adSuccess) {
                String adFive = stu.getAdFive();
                planId = tryAdmissionAndUpdateAdStatus(adFive, stu);
                adSuccess = planId == -1 ? false : true;
            }

            // 第六个志愿
            if (!adSuccess) {
                String adSix = stu.getAdSix();
                planId = tryAdmissionAndUpdateAdStatus(adSix, stu);
                adSuccess = planId == -1 ? false : true;
            }

            // 所有志愿都不能被录取
            if (!adSuccess) {
                // 查看该学生是否允许调剂
                if (Integer.valueOf(1).equals(stu.getIsSwap())) {
                    // 加入调剂队列
                    // 此时的学生在队列中成绩由高到低排名
                    swapDeque.offer(stu);
                } else {
                    // 更新录取状态
                    updateAdmissionStatus(2, stu);
                }
            } else {
                // 更新录取状态
                updateAdmissionStatus(1, stu);
                saveStusToAdmission(stu, planId);
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
        // 排序
        sortPlanList.sort((o1, o2) -> (int) (o2.getMinScore() - o1.getMinScore()));

        // 调剂录取
        swapAdmission(sortPlanList, swapDeque);

        // 把还在内存中的数据持久化
        stuService.updateBatchById(admittedStusList);
        saveBatch(admissionList);
        return Result.ok();
    }

    /**
     * 尝试录取并更新返回录取状态
     */
    private int tryAdmissionAndUpdateAdStatus(String ad, Stu stu) {
        boolean adSuccess = false;
        Plan plan = null;
        if (ad != null) {
            // 获取学生志愿计划
            plan = planService.getOne(new LambdaUpdateWrapper<Plan>().eq(Plan::getProfessionNum, ad));
            // 判断是否能够录取
            adSuccess = isSuccessAdmission(plan.getId(), stu);
        }
        // 录取则直接录入
        if (adSuccess) {
            plan = planMap.get(plan.getId());
            // 更新内存中的录取计划
            normalAdmission(plan, stu);
            return plan.getId();
        }
        return -1;
    }

    /**
     * 更新学生的录取状态
     *
     * @param status
     * @param stu
     * @param planId
     */
    private void updateAdmissionStatus(Integer status, Stu stu) {
        // 录取, 将学生插入录取队列中, 以方便批量更新
        stu.setStatus(status);
        admittedStusList.add(stu);
        if (admittedStusList.size() >= BATCH_COUNT) {
            // 批量更新
            stuService.updateBatchById(admittedStusList);
            admittedStusList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 将学生插入录取表中
     *
     * @param stu
     * @param planId
     */
    private void saveStusToAdmission(Stu stu, Integer planId) {
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
            Stu stu = swapDeque.pop();

            boolean isSuccess = false;
            for (SortPlan sortPlan : sortPlanList) {
                // 最高分数的调剂专业,判断是否能录取
                isSuccess = isSuccessAdmission(sortPlan.getPlanId(), stu);
                if (isSuccess) {
                    // 该学生加入录取队列
                    updateAdmissionStatus(3, stu);
                    saveStusToAdmission(stu, sortPlan.getPlanId());
                    updatePlanNums(sortPlan.getPlanId());
                    break;
                }
            }
            // 不能被录取, 放入退档队列
            if (!isSuccess) {
                updateAdmissionStatus(2, stu);
            }
        }
    }

    /**
     * 调剂录取, 处理 内存中的录取情况
     *
     * @param planId
     * @return
     */
    private void updatePlanNums(Integer planId) {
        Plan plan = planMap.get(planId);
        plan.setPlanNum(plan.getPlanNum() - 1);
        if (plan.getPlanNum() <= 0) {
            planMap.remove(planId);
            return;
        }
        planMap.put(planId, plan);
    }

    /**
     * 正常录取, 处理 内存中的录取情况
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
     * @return
     */
    private boolean isSuccessAdmission(Integer planId, Stu stu) {
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
        Map<Integer, ProfessionDto> professionDtoMap = new HashMap<>();
        List<Plan> planList = planService.list();
        List<ProfessionDto> records = new LinkedList<>();
        // 计算出当前页起始位置
        int start = (currentPage - 1) * pageSize;
        // 填充进map
        for (int i = start; i < start + pageSize && i < planList.size(); i++) {
            Plan plan = planList.get(i);
            ProfessionDto professionDto = new ProfessionDto();
            professionDto.setProfessionNum(plan.getProfessionNum());
            professionDto.setProfessionName(plan.getProfessionName());
            professionDto.setCollegeName(plan.getCollegeName());
            professionDtoMap.put(plan.getId(), professionDto);

            // 填充最高分和最高排位
            Admission admission = admissionMapper.queryProfessionMaxRank(plan.getId());
            if (admission != null){
                Stu stu = stuService.getById(admission.getStuId());
                professionDto.setMaxScore(stu.getScore());
                professionDto.setMaxRank(stu.getStuRank());
            }

            // 填充最低分和最低排位
            admission = admissionMapper.queryProfessionMinRank(plan.getId());
            if (admission != null) {
                Stu stu = stuService.getById(admission.getStuId());
                professionDto.setMinScore(stu.getScore());
                professionDto.setMinRank(stu.getStuRank());
            }
            // 填充平均分
            Double avgScore = admissionMapper.queryProfessionAvgScore(plan.getId());
            if (avgScore != null) {
                professionDto.setAvgScore(avgScore);
            };

            records.add(professionDto);
        }

        // 分页
        MyPage<ProfessionDto> professionDtoPage = new MyPage<>(currentPage, pageSize);
        professionDtoPage.setRecords(records);
        professionDtoPage.setTotal(planList.size());
        return Result.ok(professionDtoPage);
    }

    @Override
    public Result collegeIndex(int currentPage, int pageSize) {
        // 查询每个学院的最高分, 每个学院的最低分, 每个学院的平均分,
        Map<String, CollegeDto> collegeDtoMap = new HashMap<>();
        List<Plan> planList = planService.list();
        // 填充进map
        for (Plan plan : planList) {
            CollegeDto collegeDto = new CollegeDto();
            String collegeName = plan.getCollegeName();
            collegeDto.setCollegeName(collegeName);
            collegeDtoMap.put(collegeName, collegeDto);
        }

        // 填充最高分和最高排位
        for (Map.Entry<String, CollegeDto> collegeDtoEntry : collegeDtoMap.entrySet()) {
            // 根据学院名查询专业
            List<Integer> planIds = admissionMapper.queryProfessionIdsByCollegeName(collegeDtoEntry.getKey());
            if (planIds != null) {
                for (Integer planId : planIds) {
                    // 查询到的专业的最高排名
                    Admission admission = admissionMapper.queryProfessionMaxRank(planId);
                    if (admission == null) continue;
                    Stu stu = stuService.getById(admission.getStuId());
                    // 比较该专业所在学院的最高排名
                    CollegeDto collegeDto = collegeDtoEntry.getValue();
                    if (collegeDto.getMaxRank() == null || collegeDto.getMaxRank() > stu.getStuRank()) {
                        collegeDto.setMaxRank(stu.getStuRank());
                        collegeDto.setMaxScore(stu.getScore());
                        collegeDtoMap.put(collegeDtoEntry.getKey(), collegeDto);
                    }
                }
            }
        }

        // 填充最低分和最低排位
        for (Map.Entry<String, CollegeDto> collegeDtoEntry : collegeDtoMap.entrySet()) {
            // 根据学院名查询专业
            List<Integer> planIds = admissionMapper.queryProfessionIdsByCollegeName(collegeDtoEntry.getKey());
            if (planIds != null) {
                for (Integer planId : planIds) {
                    // 查询到的专业的最低排名
                    Admission admission = admissionMapper.queryProfessionMinRank(planId);
                    if (admission == null) continue;
                    Stu stu = stuService.getById(admission.getStuId());
                    // 比较该专业所在学院的最低排名
                    CollegeDto collegeDto = collegeDtoEntry.getValue();
                    if (collegeDto.getMinRank() == null || collegeDto.getMinRank() < stu.getStuRank()) {
                        collegeDto.setMinRank(stu.getStuRank());
                        collegeDto.setMinScore(stu.getScore());
                        collegeDtoMap.put(collegeDtoEntry.getKey(), collegeDto);
                    }
                }
            }
        }

        // 填充平均分
        for (Map.Entry<String, CollegeDto> collegeDtoEntry : collegeDtoMap.entrySet()) {
            // 根据学院名查询专业
            List<Integer> planIds = admissionMapper.queryProfessionIdsByCollegeName(collegeDtoEntry.getKey());
            if (planIds != null) {
                Double avgCollegeScore = 0.0;
                int num = 0;
                for (Integer planId : planIds) {
                    Double avgAdmissionScore = admissionMapper.queryProfessionAvgScore(planId);
                    if (avgAdmissionScore == null) continue;
                    num++;
                    avgCollegeScore += avgAdmissionScore;
                }
                avgCollegeScore /= num;
                CollegeDto collegeDto = collegeDtoEntry.getValue();
                collegeDto.setAvgScore(avgCollegeScore);
                collegeDtoMap.put(collegeDtoEntry.getKey(), collegeDto);
            }
        }

        // 分页
        Collection<CollegeDto> records = collegeDtoMap.values();
        MyPage<CollegeDto> collegeDtoPage = new MyPage<>(currentPage, pageSize);
        collegeDtoPage.setPageRecords(records);
        return Result.ok(collegeDtoPage);
    }

    @Override
    public Result schoolIndex() {
        SchoolDto schoolDto = new SchoolDto();
        // 统计全校成绩中位数
        // 查询录取的学生和调剂的学生
        List<Stu> stuList = stuService.list(new LambdaUpdateWrapper<Stu>().eq(Stu::getStatus, 1).or().eq(Stu::getStatus, 3));
        stuList.sort(new Comparator<Stu>() {
            @Override
            public int compare(Stu o1, Stu o2) {
                return (int) (o2.getStuRank() - o1.getStuRank());
            }
        });
        Double midScore = 0.0;
        // 5 个的话起始下标为2
        // 4 个的话下标为 1, 2
        int start = 0;
        int size = stuList.size();
        if (size % 2 == 1) {
            start = size / 2;
            midScore = stuList.get(start).getScore();
        } else {
            start = size / 2 - 1;
            midScore = (stuList.get(start).getScore() + stuList.get(start + 1).getScore()) / 2;
        }
        schoolDto.setMidScore(midScore);
        schoolDto.setMaxRank(stuList.get(size - 1).getStuRank());
        schoolDto.setMaxScore(stuList.get(size - 1).getScore());
        schoolDto.setMinScore(stuList.get(0).getScore());
        schoolDto.setMinRank(stuList.get(0).getStuRank());
        Double sumScore = 0.0;
        for (Stu stu : stuList) {
            sumScore += stu.getScore();
        }
        schoolDto.setAvgScore(sumScore /= stuList.size());
        return Result.ok(schoolDto);
    }

    @Override
    public Result backIndex(int currentPage, int pageSize) {
        Page<Stu> stuPage = new Page<>(currentPage, pageSize);
        stuService.page(stuPage, new LambdaQueryWrapper<Stu>().eq(Stu::getStatus, 2));
        return Result.ok(stuPage);
    }

    @Override
    public Result getStuAdmissionByParams(AdmissionStuDto admissionStuDto, int currentPage, int pageSize) {
        if(admissionStuDto == null){
            admissionStuDto = new AdmissionStuDto();
        }
        List<AdmissionStuDto> admissionStuDtoList = stuService.getAdStuByParams(admissionStuDto, currentPage, pageSize);
        // 分页
        MyPage<AdmissionStuDto> admissionStuDtoPage = new MyPage<>(currentPage, pageSize);
        admissionStuDtoPage.setRecords(admissionStuDtoList);
        admissionStuDtoPage.setTotal(admissionStuDtoList.size());
        return Result.ok(admissionStuDtoPage);
    }

    @Override
    public Result getTheBestStus(AdmissionStuDto admissionStuDto, int currentPage, int pageSize) {
        if(admissionStuDto == null){
            admissionStuDto = new AdmissionStuDto();
        }
        List<AdmissionStuDto> admissionStuDtoList = stuService.getAdStuByParams(admissionStuDto, 1, (int)(count() * 0.01));
        // 分页
        MyPage<AdmissionStuDto> admissionStuDtoPage = new MyPage<>(currentPage, pageSize);
        admissionStuDtoPage.setPageRecords(admissionStuDtoList);
        admissionStuDtoPage.setTotal((int)(count() * 0.01));
        return Result.ok(admissionStuDtoPage);
    }

}
