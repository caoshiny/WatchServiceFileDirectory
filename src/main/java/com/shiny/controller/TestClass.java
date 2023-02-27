package com.shiny.controller;

import com.shiny.entity.FileCopyFailureEntity;
import com.shiny.entity.LeoDataEntity;
import com.shiny.service.LeoDataService;
import com.shiny.utils.GetPropertiesInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestClass {
    @Autowired
    private GetPropertiesInfo getPropertiesInfo;

    @Autowired
    private LeoDataService leoDataService;

    @RequestMapping("/1")
    public void Test1(){
        List<LeoDataEntity> leoDataEntities = leoDataService.list();
        System.out.println(leoDataEntities);
    }
}
