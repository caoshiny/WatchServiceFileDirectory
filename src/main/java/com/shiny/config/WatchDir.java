package com.shiny.config;

import com.shiny.thread.CallBackRunnable;
import com.shiny.thread.CallBackThread;
import com.shiny.utils.FileTransferUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 核心类
 */
@Slf4j
@DependsOn(value = "beanContext")
public class WatchDir {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean subDir;
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 构造方法
     * @param file 文件目录，不可以是文件
     * @param subDir 控制监控是否包含子目录
     */
    // public WatchDir(File file, boolean subDir, FileActionCallback callback) throws Exception {
    public WatchDir(File file, boolean subDir) throws Exception {
        if (!file.isDirectory()) {
            throw new Exception(file.getAbsolutePath() + "不是文件夹!");
        }

        // 获取监听服务 watcher
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.subDir = subDir;

        // dir 赋予需要监控的文件路径
        Path dir = Paths.get(file.getAbsolutePath());

        if (subDir) {
            // 监控指定目录包括子目录
            registerAll(dir);
        } else {
            // 只监控指定目录不包含子目录
            register(dir);
        }
        // processEvents(callback);
        processEvents();
    }

    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * 注册监听时间
     * 只观察指定的目录
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * 观察指定的目录，并且包括子目录
     */
    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 发生文件变化的回调函数
     */
    // void processEvents(FileActionCallback callback) throws IOException, ParseException, InterruptedException {
    void processEvents() throws IOException, ParseException, InterruptedException {
        while(true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
            Path dir = keys.get(key);
            if (dir == null) {
                log.error("操作未识别");
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                // 事件可能丢失或遗弃
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // 目录内的变化可能是文件或者目录
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                File file = child.toFile();


                // || kind.name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name())
                if (kind.name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
                    if (file.isFile()){
                        // FileTransferUtils.waitForFileTransfer(file.getAbsolutePath(), 10L);

//                        while(true) {
//                            try{
//                                FileInputStream fis = new FileInputStream(file);
//                                fis.close();
//                                break;
//                            } catch (Exception e){
//                                log.error("<-主程序-> 文件正在被占用！");
//                                Thread.sleep(2000L);
//                            }
//
//                        }



//                        CallBackThread callBackThread = new CallBackThread(file, callback);
//                        callBackThread.start();
                        executor.submit(new CallBackRunnable(file));

                        // callback.create(file);
                    }
                }

                // 监控子目录
                if (subDir && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        try {
                            registerAll(child);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            // 重设WatchKey
            boolean valid = key.reset();
            if (!valid) {
                // 移除不可访问的目录
                // 因为有可能目录被移除，就会无法访问
                keys.remove(key);
                // 如果待监控的目录都不存在了，就中断执行
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
