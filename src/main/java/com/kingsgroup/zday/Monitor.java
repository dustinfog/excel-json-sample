package com.kingsgroup.zday;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Monitor extends Thread{
    private WatchService watchService;

    public void startWatch() throws IOException, InterruptedException {
        Log.log.info("Monitor started ");
        this.watchService = FileSystems.getDefault().newWatchService();
        for (String file : Utils.getWatchPaths()) {
            Path dir = Paths.get(file);
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                Path path = (Path)event.context();
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    Path watchable = ((Path)key.watchable()).resolve(path);
                    if (Files.isDirectory(watchable)) {
                        watchNewDir(watchable);
                    }
                }
                String fileName = path.getFileName().toString();
                if (fileName.equals("HEAD")) {
                    String newGit = Utils.getGitHead();
                    GitChangeEvent gitChangeEvent = new GitChangeEvent(newGit);
                    FileManager.getInstance().addEvent(gitChangeEvent);
                } else if (fileName.endsWith(".json") && !fileName.startsWith("~")) {
                    //git冲突了,有冲突的json数据不往excel里写
                    if (Utils.checkGitConflict()) {
                        Log.log.error("json git file {} conflict", fileName);
                        continue;
                    }
                    String changedFileName = Utils.getFileName(path.getFileName().toString());
                    JsonChangeEvent jsonChangeEvent = new JsonChangeEvent(event.kind().name(), changedFileName, Utils.getGitHead());
                    FileManager.getInstance().addEvent(jsonChangeEvent);

                } else if (!fileName.startsWith("~") && fileName.endsWith(".xlsx")) {
                    String changedFileName = Utils.getFileName(path.getFileName().toString());
                    Path dir = (Path)key.watchable();
                    Path fullPath = dir.resolve(fileName);
                    ExcelChangeEvent changeEvent = new ExcelChangeEvent(event.kind().name(), changedFileName);
                    changeEvent.sourcesFile = fullPath.toString();
                    FileManager.getInstance().addEvent(changeEvent);
                }
            }
            key.reset();
        }
    }

    private void watchNewDir(Path newPath) throws IOException {
        newPath.register(watchService, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        File[] files = newPath.toFile().listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                watchNewDir(file.toPath());
            }
        }
    }

    @Override
    public void run() {
        try {
            startWatch();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
