package com.shiny.config;

import com.shiny.entity.FileCopyFailureEntity;
import com.shiny.entity.LeoDataEntity;
import com.shiny.service.FileCopyFailureService;
import com.shiny.service.LeoDataService;
import com.shiny.utils.BeanContext;
import com.shiny.utils.GetPropertiesInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
// @DependsOn(value = "beanContext")
public class FileActionCallback {
    // 通过上下文工具类获取service
    private final LeoDataService leoDataService = BeanContext.getBean(LeoDataService.class);
    private final FileCopyFailureService fileCopyFailureService = BeanContext.getBean(FileCopyFailureService.class);
    private final GetPropertiesInfo getPropertiesInfo = BeanContext.getBean(GetPropertiesInfo.class);

    /**
     * 新增文件的解析和入库操作
     */
    public void create(File file) throws IOException, ParseException, InterruptedException {
        log.info("<-主程序-> 监控到有新文件! 文件的绝对路径为:\t" + file.getAbsolutePath());

        String file_type = null;
        String fileAllName = file.getName();
        String fileName = fileAllName.substring(0, fileAllName.length() - 4);
        String fileFormat = fileAllName.substring(fileAllName.length() - 3);


        // 判断文件类型
        try {
            if (fileFormat.equals("txt")) {
                file_type = "0";
            } else if (fileFormat.equals("png") || fileFormat.equals("gif") || fileFormat.equals("jpg") || fileFormat.equals("jpeg")) {
                file_type = "1";
            } else if (fileFormat.equals("mp4") || fileFormat.equals("AVI") || fileFormat.equals("WMV") || fileFormat.equals("avi") || fileFormat.equals("wmv")) {
                file_type = "2";
            }
        }catch (Exception e) {
            log.error("<-主程序-> " + "文件类型判断失败，错误信息为: " + e);
        }

        // 解析文件名，以下划线分割文件名保存在数组中
        String[] splitFileName = fileName.split("_");
        String device_id = splitFileName[0];
        String Userid = splitFileName[1];
        Double longitude = Double.parseDouble(splitFileName[2]);
        Double latitude = Double.parseDouble(splitFileName[3]);

        // 解析时间格式
        String fileDataString = splitFileName[4];
        Date date1 = new SimpleDateFormat("yyyyMMddHHmmss").parse(fileDataString);
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date1);
        Timestamp gather_time = Timestamp.valueOf(dateStr);

        String gather_description = splitFileName[5];

        // 进行入库操作
        LeoDataEntity leoDataEntity = new LeoDataEntity();
        leoDataEntity.setDevice_id(device_id);
        leoDataEntity.setUserid(Userid);
        leoDataEntity.setLongitude(longitude);
        leoDataEntity.setLatitude(latitude);
        leoDataEntity.setGather_time(gather_time);
        leoDataEntity.setFile_path(file.getAbsolutePath());
        leoDataEntity.setFile_name(fileName);
        leoDataEntity.setFile_type(file_type);
        Thread.sleep(1000);
        leoDataEntity.setFile_size(file.length());
        leoDataEntity.setFile_copy_flag("0");
        leoDataEntity.setGather_description(gather_description);

        // 判断文件是否需要拷贝到指定目录下
        if(getPropertiesInfo.getIsneedcopy()) {
            try {
                for(int i = 0; i < getPropertiesInfo.getPaths().size(); i++) {
                    FileUtils.copyFileToDirectory(file, new File(getPropertiesInfo.getPaths().get(i)));
                    log.info("<-主程序-> 文件拷贝成功！文件按已经拷贝到" + getPropertiesInfo.getPaths().get(i) + "目录下。");
                }
                leoDataEntity.setFile_copy_flag("1");
                // 获取拷贝时间插入数据库
                Timestamp insertTime = new Timestamp(new Date().getTime());
                leoDataEntity.setFile_copy_time(insertTime);
            } catch (IOException ioException){
                // 日志记录错误信息
                log.error("<-主程序-> " + "文件" + file.getAbsolutePath() + "第一次拷贝失败！稍后等待守护程序进行拷贝！");
                log.error("<-主程序-> " + "失败原因: " + ioException);

                // 把拷贝失败的文件绝对路径入库
                FileCopyFailureEntity fileCopyFailureEntity = new FileCopyFailureEntity();
                fileCopyFailureEntity.setFilePath(file.getAbsolutePath());
                fileCopyFailureService.save(fileCopyFailureEntity);
                log.info("<-主程序-> 拷贝失败文件: " + file.getAbsolutePath() + " 已经录入到拷贝失败表！");
            }
        }

        // 数据库保存操作
        try{
            leoDataService.save(leoDataEntity);
        } catch (Exception e){
            log.error("<-主程序->" + "文件" + file.getAbsolutePath() + "录入到数据库失败 ！");
        }
        log.info("<-主程序-> " + "文件" + file.getAbsolutePath() + "已经录入到数据库！");
    }
}
