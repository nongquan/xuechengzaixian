package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import io.swagger.annotations.ApiOperation;

public interface CmsPageControllerApi {
    //查询
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) ;
    //新增
    //@ApiOperation("添加页面")
    public CmsPageResult add(CmsPage cmsPage);
    //通过ID查询
    public CmsPage query(String id);
    //更新页面
    public CmsPageResult edit(String id,CmsPage cmsPage);
    //删除信息
    public ResponseResult delete(String id);
    //保存页面
    public CmsPageResult save(CmsPage cmsPage);
    //一键发布
    public  String publish(CmsPage cmsPage);
}
