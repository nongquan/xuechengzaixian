package com.xuecheng.search.controller;

import com.xuecheng.api.search.SearchControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.response.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search/course")
public class SearchController implements SearchControllerApi {
    @Autowired
    SearchService searchService;
    //搜索结果
    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult<CoursePub> search(@PathVariable("page") int page,@PathVariable("size") int size, CourseSearchParam courseSearchParam) {

        return searchService.search(page,size,courseSearchParam);
    }
    //视频扩展
    @GetMapping("/getall/{id}")
    public Map<String,CoursePub> search(@PathVariable("id") String id) {
        return searchService.search(id);
    }

    //根据课程teachplan——id 查询索引库  表xc_course_medic
    @Override
    @GetMapping("/getmedia/{teachplanId}")
    public TeachplanMediaPub seachTeachplanMediaPub(@PathVariable("teachplanId") String teachplanId) {

        return searchService.seachTeachplanMediaPub( teachplanId);
    }
}
