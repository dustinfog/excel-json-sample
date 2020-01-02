package com.kingsgroup.zday;

import java.io.File;
import java.io.IOException;

public class JsonChangeEvent extends BasicEvent {
    private String fileName;

    public JsonChangeEvent(String eventType, String fileName, String gitHead) {
        super((Utils.getCurrPathJson() + File.separator + fileName + ".json"),
                (Utils.getCurrPathExcel()+File.separator+gitHead+File.separator+fileName+".xlsx"),
                eventType);
        this.fileName = fileName;
    }

    @Override
    public boolean checkEffective() throws IOException {
        Record record = (Record) LevelDB.getInstance().get(targetFile);
        //没有自己变化的记录，说明是新文件变化，不需要去校验
        if (record == null) {
            return true;
        }
        String sfMD5 = Utils.calcFileMD5(new File(sourcesFile));
        this.record = record;
        //说明当前的修改事件是自己本地文件变化修改了目标文件，导致目标文件触发变化来反向修改自己的事件，该事件直接丢弃
        return !sfMD5.equals(record.MD5);
    }

    @Override
    public boolean process() {
        try {
            super.process();
            if (this.eventType.equals(BasicEvent.ENTRY_DELETE)) {
                LevelDB.getInstance().delete(targetFile);
                return true;
            }
            boolean flag = Utils.transferJsonToExcel(sourcesFile, targetFile, fileName);
            if (!flag) {
                return false;
            }
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
        Log.log.info("excel file change success "+fileName);
        return true;
    }
}
