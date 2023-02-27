package com.shiny.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiny.dao.FileCopyFailureDao;
import com.shiny.entity.FileCopyFailureEntity;
import com.shiny.service.FileCopyFailureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FileCopyFailureServiceImpl extends ServiceImpl<FileCopyFailureDao, FileCopyFailureEntity> implements FileCopyFailureService {
}
