package com.kingsgroup.zday;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class FileManager {
    private static FileManager instance = new FileManager();

    private final BlockingDeque<BasicEvent> queue = new LinkedBlockingDeque<>();

    public volatile boolean isRunning;

    private List<BasicEvent> bufferEventList = new LinkedList<>();//缓冲数据

    private String gitHeadInfo;

    private FileManager() {
        isRunning = true;
        try {
            this.gitHeadInfo = Utils.getGitHead();
            Log.log.info("current git head is: [" + this.gitHeadInfo + "]");
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    public static FileManager getInstance() {
        return instance;
    }

    public void addEvent(BasicEvent event) {
        Log.log.info("add new event:" + event.toString());
        queue.offer(event);
    }

    public String getGitHeadInfo() {
        return this.gitHeadInfo;
    }

    public void setGitHeadInfo(String gitHeadInfo) {
        this.gitHeadInfo = gitHeadInfo;
    }

    public void addBufferEvent(Collection<BasicEvent> list) {
        bufferEventList.addAll(list);
    }

    public void run() {
        while (isRunning || !queue.isEmpty()) {
            try {
                if (bufferEventList.isEmpty()) {
                    queue.drainTo(bufferEventList);
                }
                if (bufferEventList.isEmpty()) {
                    Thread.sleep(100);
                }
                ListIterator<BasicEvent> iterator = bufferEventList.listIterator();
                while (iterator.hasNext()) {
                    BasicEvent event = iterator.next();
                    if (!event.checkEffective()) {
                        Log.log.info("not effective Change discard：" + event.sourcesFile);
                        iterator.remove();
                        continue;
                    }
                    if (!event.process()) {
                        Thread.sleep(2000);
                        break;
                    }
                    event.updateRecord();
                    iterator.remove();
                }
                bufferEventList.clear();
            } catch (Throwable th) {
                Log.log.error("error", th);
            }
        }
    }
}
