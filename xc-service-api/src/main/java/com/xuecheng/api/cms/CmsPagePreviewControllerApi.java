package com.xuecheng.api.cms;

import org.springframework.web.bind.annotation.PathVariable;

public interface CmsPagePreviewControllerApi{
    //向页面输出静态化页面
    public void Preview(String pageId);
}
