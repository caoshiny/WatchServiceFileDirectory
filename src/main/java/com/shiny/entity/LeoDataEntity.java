package com.shiny.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("leo_data_gather")
public class LeoDataEntity {
    @TableId(type = IdType.AUTO)
    private int Id;

    /**
     * 表中的其他字段
     */
    private String device_id;
    private String Userid;
    private String Nickname;
    private Double longitude;
    private Double latitude;
    private Timestamp gather_time;
    private String file_path;
    private String file_name;
    private String file_type;
    private long file_size;
    private String File_copy_flag;
    private Timestamp File_copy_time;
    private String gather_description;
}
