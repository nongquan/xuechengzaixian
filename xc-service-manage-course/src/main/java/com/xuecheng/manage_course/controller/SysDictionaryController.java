package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.SysDictionaryControllerApi;
import com.xuecheng.framework.domain.course.ext.SysDictionary;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SysDictionaryController implements SysDictionaryControllerApi {

    @Autowired
    CourseService courseService;

    //课程等级，学习模式
    @Override
    @GetMapping("/sys/dictionary/get/{type}")
    public SysDictionary findDictionaryByType(@PathVariable("type") String type) {

        return courseService.findDictionaryByType(type);
    }
}
