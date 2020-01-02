package com.kingsgroup.zday;

import org.apache.commons.lang3.builder.ToStringBuilder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class GitChangeEvent extends BasicEvent {
    private String gitHeadInfo;
    public GitChangeEvent(String gitHeadInfo) {
        super();
        this.gitHeadInfo = gitHeadInfo;
    }
    @Override
    public void updateRecord() {}

    @Override
    public boolean process() {
        super.process();
        String oldValue = FileManager.getInstance().getGitHeadInfo();
        if (!oldValue.equals(gitHeadInfo)) {
            Log.log.info("Change git to->["+gitHeadInfo+"]");
            FileManager.getInstance().setGitHeadInfo(gitHeadInfo);
            ExcelChangeRecords records = (ExcelChangeRecords) LevelDB.getInstance().get(gitHeadInfo);
            if (records != null) {
                Collection<BasicEvent> list = new LinkedList<>();
                for (Map.Entry<String, String> entry : records.records.entrySet()) {
                    ExcelChangeEvent event = new ExcelChangeEvent(entry.getKey(), entry.getValue(), gitHeadInfo);
                    list.add(event);
                }
                FileManager.getInstance().addBufferEvent(list);
            }
        }
        return true;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("headInfo", gitHeadInfo)
                .toString();
    }
}
