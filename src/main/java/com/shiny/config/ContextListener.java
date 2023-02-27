package com.shiny.config;

import com.shiny.utils.BeanContext;
import com.shiny.utils.GetFolderPaths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
@Slf4j
@DependsOn(value = "beanContext")
public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        /**
         * file 变量保存的是需要监控的文件目录而不是文件本身
         * 一个线程负责监控一个文件夹
         */
        // 此线程用来等待上下文工具类加载
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                GetFolderPaths getFolderPaths = BeanContext.getBean(GetFolderPaths.class);
                // 根据需要监控文件夹的数量开启线程
                Thread[] threads = new Thread[getFolderPaths.getPaths().size()];
                for(int i = 0; i < getFolderPaths.getPaths().size(); i ++){
                    threads[i] = new MyThread(getFolderPaths.getPaths().get(i));
                }
                for(Thread thread : threads) {
                    thread.start();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
