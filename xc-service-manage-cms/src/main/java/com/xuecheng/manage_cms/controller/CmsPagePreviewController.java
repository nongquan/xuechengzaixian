package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPagePreviewControllerApi;
import com.xuecheng.framework.model.response.Response;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/cms")
public class CmsPagePreviewController extends BaseController implements CmsPagePreviewControllerApi {
    @Autowired
    PageService pageService;
    //向页面输出静态化页面
    @GetMapping("/preview/{pageId}")
    public void Preview(@PathVariable("pageId") String pageId){
        String pageHtml = pageService.getPageHtml(pageId);
        if (StringUtils.isNotBlank(pageHtml)){
            try {
                ServletOutputStream outputStream = response.getOutputStream();
                response.setHeader("Content-type","text/html;charset=utf-8");
                outputStream.write(pageHtml.getBytes("utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
