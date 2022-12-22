package com.gdut.admission.dto;

import lombok.Data;

import java.util.*;

@Data
public class MyPage<T>{
    private Integer currentPage;
    private Integer pageSize;
    private Integer total;
    private List<T> records;

    // 手动分页
    public void setPageRecords(Collection<T> collection){
        List<T> resList = new ArrayList<>();

        // 手动分页
        int total = collection.size();
        // 起始下标
        int start = (currentPage - 1) * pageSize + 1;
        int i = 1;
        int size = pageSize;
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()){
            if(i++ == start){
                while(size-- > 0 && iterator.hasNext()){
                    resList.add(iterator.next());
                }
                break;
            }
            iterator.next();
        }
        records = resList;
        this.total = records.size();
    }

    public MyPage(Integer currentPage, Integer pageSize, Integer total) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.total = total;
    }

    public MyPage(Integer currentPage, Integer pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public MyPage() {
    }

    public MyPage(Integer currentPage, Integer pageSize, Integer total, List<T> records) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
    }
}
