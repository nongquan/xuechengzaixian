package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.ext.*;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/course")
public class CourseController  implements CourseControllerApi {
    @Autowired
    CourseService courseService;
    //课程计划
    /*@PreAuthorize("hasAuthority('course_find_list')")*/
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findNodeById(@PathVariable("courseId") String courseId){

        return courseService.findNodeById(courseId);
    }
    //添加课程计划
    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTecachplan(@RequestBody Teachplan teachplan) {

        return courseService.addTecachplan(teachplan);
    }
    //课程分页查询
   /* @PreAuthorize("hasAuthority('coursebase_list')")*/
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult findCourseBase(@PathVariable("page") int page, @PathVariable("size") int size, CourseListRequest courseListRequest) {
        //用户登录后 根据公司id不一样 取到的课程也不一样  companyid通过解析令牌里的信息 拿到
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        XcOauth2Util.UserJwt userJwtFromHeader = xcOauth2Util.getUserJwtFromHeader(request);
        String companyId = userJwtFromHeader.getCompanyId();

        return courseService.findCourseBase(page,size,companyId,courseListRequest);
    }
    //新增课程
    @PostMapping("/coursebase/add")
    public ResponseResult saveCourseBase(@RequestBody CourseBase courseBase) {

        return courseService.saveCourseBase(courseBase);
    }
    //新增课程图片
    @PostMapping("/coursepic/add")
    public ResponseResult savePicture(String courseId, String pic) {
        return courseService.savePicture(courseId,pic);
    }
    //回显课程图片
    @Override
   /* @PreAuthorize("hasAuthority('coursepic_list')")*/
    @GetMapping("/coursepic/list/{courseid}")
    public CoursePic findPicture(@PathVariable("courseid") String courseid) {

        return courseService.findPicture(courseid);
    }
    //删除课程图片
    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deletePicture(String courseId) {
        return courseService.deletePicture(courseId);
    }
    //页面模板静态化数据
    @GetMapping("/courseview/{courseid}")
    public CourseView getCourseViewById(@PathVariable("courseid") String courseid) {
        return courseService.getCourseViewById(courseid);
    }

    //拿取URL
    @PostMapping("/preview/{courseid}")
    public CoursePublishResult findUrlByCourseid(@PathVariable("courseid") String courseid){

        return courseService.findUrlByCourseid(courseid);
    }

    //一键课程发布
    @Override
    @PostMapping("/publish/{courseid}")
    public CoursePublishResult publish(@PathVariable("courseid") String courseid) {
        return courseService.publish(courseid);
    }
    //给课程绑定视频
    @Override
    @PostMapping("/savemedia")
    public ResponseResult saveMedia(@RequestBody TeachplanMedia teachplanMedia) {

        return courseService.saveMedia(teachplanMedia);
    }

}

