package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Administrator.
 */
public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser,String> {
    XcCompanyUser findByUserId(String userId);
}
