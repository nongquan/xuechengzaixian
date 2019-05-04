package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.ext.*;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

import java.util.List;

public interface CourseControllerApi {
    //课程计划
    public TeachplanNode findNodeById(String courseId);
    //添加课程计划
    public ResponseResult addTecachplan(Teachplan teachplan);
    //课程分页查询
    public QueryResponseResult findCourseBase(int page, int size, CourseListRequest courseListRequest);
    //新增课程
    public ResponseResult saveCourseBase(CourseBase courseBase);
    //新增课程图片
    public ResponseResult savePicture(String courseId,String pic);
    //回显课程图片
    public CoursePic findPicture(String courseid);
    //删除课程图片
    public ResponseResult deletePicture(String courseId);
    //页面模板静态化数据
    public CourseView getCourseViewById(String courseid);
    //一键课程发布
    public CoursePublishResult publish(String courseid);
    //给课程绑定视频
    public ResponseResult  saveMedia(TeachplanMedia teachplanMedia);

}
