package com.kingsgroup.zday;

import org.apache.commons.lang3.builder.ToStringBuilder;
import java.io.Serializable;

public class Record implements Serializable {
    String fileFullName;
    String MD5;
    long changeTime;
    String gitHeadInfo;

    public Record(String fileName, String MD5) {
        this.fileFullName = fileName;
        this.MD5 = MD5;
        this.changeTime =  System.currentTimeMillis();
        this.gitHeadInfo = FileManager.getInstance().getGitHeadInfo();
    }

    public void setGitHeadInfo(String gitHeadInfo) {
        this.gitHeadInfo = gitHeadInfo;
    }


    public String toString() {
        return new ToStringBuilder(this)
                .append("fileFullName", fileFullName)
                .append("MD5", MD5)
                .append("changeTime", changeTime)
                .append("gitHeadInfo", gitHeadInfo)
                .toString();
    }

}
