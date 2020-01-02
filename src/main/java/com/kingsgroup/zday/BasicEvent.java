package com.kingsgroup.zday;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;
import java.io.IOException;

public class BasicEvent {

    public static final String ENTRY_DELETE = "ENTRY_DELETE";
    public static final String ENTRY_CREATE = "ENTRY_CREATE";
    public static final String ENTRY_MODIFY = "ENTRY_MODIFY";
    /**
     * 源文件
     */
    String sourcesFile;
    /**
     * 事件类型
     */
    String eventType;

    /**
     * 变更目标文件
     */
    String targetFile;

    /**
     * 变更记录
     */
    Record record;

    public BasicEvent() {}

    public BasicEvent(String sourcesFile,String targetFile, String eventType) {
        this.sourcesFile = sourcesFile;
        this.targetFile = targetFile;
        this.eventType = eventType;
    }

    /**
     * 检查当前事件是否是有效的
     * @return boolean
     */
    public boolean checkEffective() throws IOException {
        return true;
    }

    /**
     * 每次更新操作后，记录当前源文件操作的记录
     */
    public void updateRecord() {
        if (eventType.equals(ENTRY_DELETE)) {
            return;
        }
        try {
            if (this.record == null) {
                this.record = new Record(targetFile, Utils.calcFileMD5(new File(targetFile)));
            } else {
                this.record.MD5 = Utils.calcFileMD5(new File(targetFile));
            }
            String key = sourcesFile;
            //json变化的事件记录时将git的信息拼到key中，避免切到其他git分支，原来分支记录被覆盖
            if (this instanceof JsonChangeEvent) {
                key = sourcesFile + FileManager.getInstance().getGitHeadInfo();
            }
            LevelDB.getInstance().update(key, record);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public boolean process() {
        return true;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("sourcesFile", sourcesFile)
                .append("targetFile", targetFile)
                .append("eventType", eventType)
                .toString();
    }
}
