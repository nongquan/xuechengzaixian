package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Administrator.
 */
public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse,String> {
    XcLearningCourse findByUserIdAndCourseId(String userId,String courseId);
}
