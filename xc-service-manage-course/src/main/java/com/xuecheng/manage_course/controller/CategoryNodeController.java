package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CategoryNodeApi;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/category")
public class CategoryNodeController implements CategoryNodeApi {
    @Autowired
    CourseService courseService;

    //课程分类
    @Override
    @GetMapping("/list")
    public CategoryNode findCategoryNode() {
        return courseService.findCategoryNode();
    }
}
