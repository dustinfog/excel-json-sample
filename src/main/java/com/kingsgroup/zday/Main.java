package com.kingsgroup.zday;


public class Main {

    public static void main(String[] args){
        LevelDB.getInstance().initLevelDB();
        new Monitor().start();
        FileManager.getInstance().run();
    }
}
