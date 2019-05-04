package com.xuecheng.manage_course.config;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "xc-service-manage-cms")
public interface CourseClient {
    @PostMapping("/cms/page/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage);
    @PostMapping("/cms/page/publish")
    public String publish(@RequestBody CmsPage cmsPage);
}
