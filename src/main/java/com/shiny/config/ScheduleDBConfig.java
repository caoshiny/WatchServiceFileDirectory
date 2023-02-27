package com.shiny.config;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shiny.entity.FileCopyFailureEntity;
import com.shiny.entity.LeoDataEntity;
import com.shiny.service.FileCopyFailureService;
import com.shiny.service.LeoDataService;
import com.shiny.utils.GetPropertiesInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * 守护程序
 */
@Slf4j
@Configuration
@EnableScheduling
public class ScheduleDBConfig {
    @Autowired
    FileCopyFailureService fileCopyFailureService;

    @Autowired
    LeoDataService leoDataService;

    @Autowired
    GetPropertiesInfo getPropertiesInfo;

    @Scheduled(cron = "0 0/30 * * * ?")
    //@Scheduled(cron = "0/5 * * * * ?")
    private void test1(){
        List<FileCopyFailureEntity> fileCopyFailureEntities = fileCopyFailureService.list();

        if (fileCopyFailureEntities.size() == 0){
            log.info("<-守护程序-> ****** 没有需要拷贝的文件！ ******");
        } else {
            log.info("<-守护程序-> " + "监测到拷贝失败表中有文件需要拷贝！");
            log.info("<-守护程序-> " + "需要拷贝的文件信息为: " + fileCopyFailureEntities.toString());

            // for循环依次拷贝剩下的文件
            for (FileCopyFailureEntity fileCopyFailureEntity : fileCopyFailureEntities) {
                File needCopyFile = new File(fileCopyFailureEntity.getFilePath());
                try {
                    // for循环拷贝到需要拷贝的目录下
                    for (int i = 0; i < getPropertiesInfo.getPaths().size(); i++) {
                        FileUtils.copyFileToDirectory(needCopyFile, new File(getPropertiesInfo.getPaths().get(i)));
                        log.info("<-守护程序-> 文件拷贝成功！文件按已经拷贝到" + getPropertiesInfo.getPaths().get(i) + "目录下。");
                    }
                    /**
                     * 拷贝成功 主表更新字段
                     */
                    // 获取拷贝时间插入数据库
                    Timestamp insertTime = new Timestamp(new Date().getTime());

                    // 根据文件的绝对路径更新数据库主表 两个字段 flag copytime
                    LambdaUpdateWrapper<LeoDataEntity> leoQueryWrapper = Wrappers.<LeoDataEntity>lambdaUpdate()
                            .eq(LeoDataEntity::getFile_path, fileCopyFailureEntity.getFilePath())
                            .set(LeoDataEntity::getFile_copy_flag, "1")
                            .set(LeoDataEntity::getFile_copy_time, insertTime);
                    leoDataService.update(null, leoQueryWrapper);
                    log.info("<-守护程序-> " + "由于文件拷贝成功，已更新主表中该文件拷贝相关信息！");

                    /**
                     * 拷贝成功 辅助表删除记录
                     */
                    // 文件拷贝成功就删除数据库中记录
                    fileCopyFailureService.removeById(fileCopyFailureEntity.getId());
                    log.info("<-守护程序-> " + "由于文件拷贝成功，删除拷贝失败表中该文件记录！");
                } catch (IOException ioException) {
                    // 拷贝失败保存拷贝失败文件数据库记录并日志记录错误信息
                    log.error("<-守护程序-> " + "文件拷贝失败！ 错误信息为: " + ioException);
                }
            }
        }
    }
}
