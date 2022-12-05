package com.gdut.admission.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson.JSON;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.service.IStuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
public class StuListener extends AnalysisEventListener<Stu> {

    private IStuService stuService;

    /**
     * 每隔500条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 500;

    public StuListener(IStuService stuService) {
        this.stuService = stuService;
    }

    // 装载读取到的数据
    private List<Stu> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    @Override
    public void invoke(Stu stu, AnalysisContext analysisContext) {
        cachedDataList.add(stu);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveStus();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 确保遗留的数据保存
        saveStus();
        log.info("学生志愿数据解析完成!");
    }

    private void saveStus(){
        log.info("{}条数据，开始存储数据库！", cachedDataList.size());
        stuService.saveBatch(cachedDataList);
        log.info("存储数据库成功！");
    }
}
