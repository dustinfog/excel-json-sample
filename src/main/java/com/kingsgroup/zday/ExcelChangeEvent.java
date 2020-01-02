package com.kingsgroup.zday;

import java.io.File;
import java.io.IOException;

public class ExcelChangeEvent extends BasicEvent {
    private String fileName;

    public ExcelChangeEvent(String eventType, String fileName) {
        super((Utils.getCurrPathExcel() +File.separator  + File.separator + fileName + ".xlsx"),
                Utils.getCurrPathJson() + File.separator + fileName+".json",
                eventType);
        this.fileName = fileName;
    }

    /**
     * 分支更改的事件过来时调用
     * @param eventType
     * @param fileName
     * @param gitHead
     */
    public ExcelChangeEvent(String eventType, String fileName, String gitHead) {
        super((Utils.getCurrPathExcel() +File.separator+ gitHead +File.separator + fileName + ".xlsx"),
                Utils.getCurrPathJson() + File.separator + fileName+".json",
                eventType);
        this.fileName = fileName;
    }

    @Override
    public boolean checkEffective() throws IOException {
        //没有变化的记录，说明是新文件变化，不需要去校验
        String headInfo = FileManager.getInstance().getGitHeadInfo();
        File f = new File(sourcesFile);
        //当前的excel文件的实际目录不是当前分支的目录
        if (!f.getParentFile().getName().equals(headInfo)) {
            insertOffLineChangeRecord(headInfo, fileName);
            return false;
        }
        //存储记录时json的拼接上了git的head信息
        Record record = (Record) LevelDB.getInstance().get(targetFile+headInfo);
        if (record == null) {
            return true;
        }
        //当前修改的是其他分支的excel文件
        if (!record.gitHeadInfo.equals(headInfo)) {
            insertOffLineChangeRecord(record.gitHeadInfo, fileName);
            return false;
        }
        String sfMD5 = Utils.calcFileMD5(new File(sourcesFile));
        //说明当前的修改事件是自己本地文件变化修改了目标文件，导致目标文件触发变化来反向修改自己的事件，该事件直接丢弃
        this.record = record;
        return !sfMD5.equals(record.MD5);
    }

    public void insertOffLineChangeRecord(String gitHead, String fileName) {
        ExcelChangeRecords records = (ExcelChangeRecords) LevelDB.getInstance().get(gitHead);
        if (null == records) {
            records = new ExcelChangeRecords(gitHead);
        }
        records.addRecord(fileName, eventType);
        Log.log.info("add OffLineChangeRecord: "+records.toString());
    }

    @Override
    public boolean process(){
        try {
            if (this.eventType.equals(BasicEvent.ENTRY_DELETE)) {
                LevelDB.getInstance().delete(targetFile);
                return true;
            }
            DataSheet dataSheet = DataSheet.readFromExcel(sourcesFile);
            assert dataSheet != null;
            dataSheet.writeToJson(targetFile);
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
        Log.log.info("json file change success "+fileName);
        return true;
    }
}
