package com.shiny.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("file_copy_failure")
public class FileCopyFailureEntity {
    @TableId(type = IdType.AUTO)
    private int id;
    private String filePath;
}
