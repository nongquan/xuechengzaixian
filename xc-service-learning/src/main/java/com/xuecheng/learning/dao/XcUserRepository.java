package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Administrator.
 */
public interface XcUserRepository extends JpaRepository<XcUser,String> {
    XcUser findByUsername(String username);
}
