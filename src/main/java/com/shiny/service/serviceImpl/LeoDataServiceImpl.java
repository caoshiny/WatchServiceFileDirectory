package com.shiny.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiny.dao.LeoDataDao;
import com.shiny.entity.LeoDataEntity;
import com.shiny.service.LeoDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeoDataServiceImpl extends ServiceImpl<LeoDataDao, LeoDataEntity> implements LeoDataService {
}
