package com.shiny.config;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class MyThread extends Thread {
    private final String fileDir;

    public MyThread(String fileDir){
        this.fileDir = fileDir;
    }

    @Override
    public void run(){
        try {
            File file = new File(fileDir);
            log.info("<-主程序-> 正在监控文件夹: " + file.getAbsolutePath());
            // new WatchDir(file, true, new FileActionCallback(){});
            new WatchDir(file, true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
