package com.xuecheng.api.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.response.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

import java.util.Map;

public interface SearchControllerApi {

    //搜索结果
    public QueryResponseResult<CoursePub> search(int page, int size, CourseSearchParam courseSearchParam);
    //视频扩展
    public Map<String,CoursePub> search(String courseid);
    //根据课程teachplan——id 查询索引库  表xc_course_medic
    public TeachplanMediaPub seachTeachplanMediaPub(String teachplanId);
}
