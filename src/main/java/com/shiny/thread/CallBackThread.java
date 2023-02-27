package com.shiny.thread;

import com.shiny.config.FileActionCallback;
import com.shiny.utils.FileTransferUtils;
import lombok.SneakyThrows;

import java.io.File;

public class CallBackThread extends Thread{
    private File file;
    private FileActionCallback callback;

    public CallBackThread(File file, FileActionCallback callback){
        this.file = file;
        this.callback = callback;
    }

    @SneakyThrows
    public void run() {
        FileTransferUtils.waitForFileTransfer(file.getAbsolutePath(), 10L);
        callback.create(file);
    }
}
