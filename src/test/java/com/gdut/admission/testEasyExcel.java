package com.gdut.admission;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.gdut.admission.entity.Stu;
import com.gdut.admission.listener.StuListener;
import com.gdut.admission.service.IStuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class testEasyExcel {
    @Autowired
    private IStuService stuService;

    @Test
    public void testSimpleRead(){
        String fileName = "D:\\my_workspace\\admission\\src\\main\\resources\\平行志愿测试数据.xlsx";
        ExcelReader excelReader = EasyExcel.read(
                fileName,
                Stu.class,
                new StuListener(stuService)
        ).build();
        ReadSheet readSheet = EasyExcel.readSheet(1).build();
        excelReader.read(readSheet);
        excelReader.finish();
    }
}
