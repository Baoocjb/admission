package com.gdut.admission.listener;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.gdut.admission.entity.Plan;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.service.IPlanService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class PlanListener extends AnalysisEventListener<Plan> {
    private IPlanService planService;

    /**
     * 每隔500条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 500;

    public PlanListener(IPlanService planService) {
        this.planService = planService;
    }

    // 装载读取到的数据
    private List<Plan> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    @Override
    public void invoke(Plan plan, AnalysisContext analysisContext) {
        cachedDataList.add(plan);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            savePlans();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 确保遗留的数据保存
        savePlans();
        log.info("招生计划数据解析完成!");
    }

    private void savePlans(){
        log.info("{}条数据，开始存储数据库！", cachedDataList.size());
        planService.saveBatch(cachedDataList);
        log.info("存储数据库成功！");
    }
}
