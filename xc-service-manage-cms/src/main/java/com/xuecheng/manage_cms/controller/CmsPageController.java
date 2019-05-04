package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    PageService pageService;
    //查询列表
    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page,@PathVariable("size") int size, QueryPageRequest queryPageRequest) {

        /*QueryResult<CmsPage> queryResult = new QueryResult<>();
        List<CmsPage> list = new ArrayList<>();
        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageName("页面测试");
        list.add(cmsPage);
        queryResult.setList(list);
        queryResult.setTotal(1);

         QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;*/


        return pageService.findList(page,size,queryPageRequest);
    }

    //新增
    @Override
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody  CmsPage cmsPage) {

        return pageService.add(cmsPage);
    }

    //通过ID查询
    @Override
    @GetMapping("/get/{id}")
    public CmsPage query(@PathVariable("id") String id) {

        return pageService.query(id);
    }
    //更新编辑页面
    @Override
    @PutMapping("/edit/{id}")
    public CmsPageResult edit(@PathVariable("id")String id,@RequestBody CmsPage cmsPage) {

        return pageService.update(id,cmsPage);
    }
    //删除
    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable("id") String id) {

        return pageService.delete(id);
    }
    //保存页面
    @Override
    @PostMapping("/save")
    public CmsPageResult save(@RequestBody  CmsPage cmsPage) {

        return pageService.save(cmsPage);
    }
    //一键课程发布
    @Override
    @PostMapping("/publish")
    public String publish(@RequestBody CmsPage cmsPage) {

        return pageService.publish(cmsPage);
    }

}
