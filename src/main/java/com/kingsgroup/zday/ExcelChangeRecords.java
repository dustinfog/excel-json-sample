package com.kingsgroup.zday;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ExcelChangeRecords implements Serializable {
    String gitHead;//excel的分支信息
    Map<String, String> records = new HashMap<>();//excel变更记录, key fileName value: eventType

    public ExcelChangeRecords(String gitHead) {
        this.gitHead = gitHead;
    }

    public void addRecord(String fileName, String eventType) {
        records.put(fileName, eventType);
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("gitHead", gitHead)
                .append("records", records.toString())
                .toString();
    }
}
