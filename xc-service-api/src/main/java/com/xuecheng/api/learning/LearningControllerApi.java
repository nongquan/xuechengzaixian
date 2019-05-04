package com.xuecheng.api.learning;

import com.xuecheng.framework.domain.learning.ext.MediaPubResult;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;

public interface LearningControllerApi {

    //调用搜索服务，拿取media__url
    public MediaPubResult getUrl(String courseId,String teachplanId);
    //拿取用户信息
    public XcUserExt getUser(String username);
}
