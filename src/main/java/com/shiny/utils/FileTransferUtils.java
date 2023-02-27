package com.shiny.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FileTransferUtils {
    public static void waitForFileTransfer(String filePath, long waitTime){
        try {
            File file = new File(filePath);
            long len1, len2;
            len2 = file.length();
            do {
                len1 = len2;
                Thread.sleep(waitTime);
                file = new File(filePath);
                len2 = file.length();
            } while (len1 < len2);
        } catch (Exception e) {
            log.error("<-主程序-> 文件" + filePath +"传输失败，失败原因: " + e);
        }
    }
}
