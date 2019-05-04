package com.xuecheng.manage_course.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoursepicMapper {
    public void updateCoursePic(String pic,String courseId) ;
}
