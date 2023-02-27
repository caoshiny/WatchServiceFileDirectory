package com.shiny.thread;

import com.shiny.config.FileActionCallback;
import com.shiny.utils.FileTransferUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class CallBackRunnable implements Runnable{
    private File file;

    public CallBackRunnable(File file){
        this.file = file;
    }

    @SneakyThrows
    public void run() {
        FileActionCallback callback = new FileActionCallback();
        // FileTransferUtils.waitForFileTransfer(file.getAbsolutePath(), 10L);
        while(true) {
            if(file.renameTo(file)) {
                break;
            } else {
                log.info("<-主程序-> 文件" + file.getName() + "正在传输中！");
                Thread.sleep(3000);
            }
        }

        callback.create(file);
    }
}
