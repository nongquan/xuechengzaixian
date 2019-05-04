package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.ext.SysDictionary;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@Transactional
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    CmsSiteRepository cmsSiteRepository;


    //查询列表
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
       if (queryPageRequest==null){
           queryPageRequest = new QueryPageRequest();
       }
        //条件匹配器
        //页面名称模糊查询，需要自定义字符串的匹配器实现模糊查询
        ExampleMatcher matching = ExampleMatcher.matching()
        .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());

        //设置查询条件
        CmsPage cmsPage = new CmsPage();
        if (StringUtils.isNotBlank(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (StringUtils.isNotBlank(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if (StringUtils.isNotBlank(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        Example<CmsPage> example = Example.of(cmsPage, matching);

        //分页参数
        if(page <=0){
            page = 1;
        }
        page = page -1;
        if(size<=0){
            size = 10;
        }

        PageRequest pageRequest = PageRequest.of(page, size);
       // Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageRequest);//自定义分页查询
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());//数据列表
        queryResult.setTotal(all.getTotalElements());//数据总记录数
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    //新增
    public CmsPageResult add(CmsPage cmsPage) {
        CmsPage cmsPage1 =null;


        if (cmsPage!=null){
            //三个条件并为一个索引，三个条件同时存在则查到对象，如果只有两个则查不到；
             cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        }
        //自定义异常
        if (cmsPage1!=null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        if (cmsPage1==null){
            cmsPage.setPageId(null);
            CmsPage save = cmsPageRepository.save(cmsPage);
            return new  CmsPageResult(CommonCode.SUCCESS,save);
        }
        return new  CmsPageResult(CommonCode.FAIL,null) ;
    }
    //通过ID查询
    public CmsPage query(String id){
        if (id!=null){
            Optional<CmsPage> byId = cmsPageRepository.findById(id);
            if (byId.isPresent()){
                CmsPage cmsPage = byId.get();
                return cmsPage;
            }
        }
        return  null;
    }
    //更新编辑页面
    public CmsPageResult update(String id,CmsPage cmsPage){
        CmsPage one = this.query(id);
        if (one!=null){
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //执行更新
            CmsPage save = cmsPageRepository.save(one);
            return  new  CmsPageResult(CommonCode.SUCCESS,save);
        }

        return  new CmsPageResult(CommonCode.FAIL,null);
    }
    //删除
    public ResponseResult delete(String id){
        CmsPage query = this.query(id);
        if (query!=null){
            cmsPageRepository.delete(query);

            return new ResponseResult(CommonCode.SUCCESS);
        }


        return new ResponseResult(CommonCode.FAIL);
    }
    //静态化页面
    public  String getPageHtml(String pageId){
        Map model = this.getModel(pageId);
        if (model==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        String template = this.getTemplate(pageId);
        if(StringUtils.isBlank(template)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        String html = this.getHtml(model, template);
        if (StringUtils.isNotBlank(html)){
            return html;
        }
        return null;
    }
    //拿到数据
    public Map getModel(String pageId){
        //查询页面信息
        CmsPage query = this.query(pageId);
        if (query==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //取出dataUrl
        String dataUrl = query.getDataUrl();
        if (dataUrl==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }

        //拿取令牌
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String JWT = request.getHeader("Authorization");
        if (StringUtils.isBlank(JWT)){
            ExceptionCast.cast(CommonCode.SERVER_ERROR);
        }
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization",JWT);
        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);
        ResponseEntity<Map> exchange = restTemplate.exchange(dataUrl, HttpMethod.GET, httpEntity, Map.class);
        Map map = exchange.getBody();

       /* ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map map = forEntity.getBody();*/
        return map;
    }
    //拿到模板
    public String getTemplate(String pageId){
        CmsPage query = this.query(pageId);
        if (query==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        String templateId = query.getTemplateId();
        if (templateId==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        /*Optional<CmsTemplate> byId = */
        CmsTemplate byTemplateFileId = cmsTemplateRepository.findByTemplateFileId(templateId);
        if (byTemplateFileId!=null){
           // CmsTemplate cmsTemplate = byId.get();
            String templateFileId = byTemplateFileId.getTemplateFileId();
            //取出模板文件内容
            GridFSFile gridFSFile = gridFsTemplate.findOne((Query.query(Criteria.where("_id").is(templateFileId))));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            try {
                String s = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return s;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        return null;
    }
    //页面静态化
    public String getHtml(Map model,String template){
        try {
            //生成配置类 FreeMarket版的
            Configuration configuration = new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template",template);
            //配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);
            //获取模板
            Template template1 = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //保存页面
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage cmsPage1 =null;


        if (cmsPage!=null){
            //三个条件并为一个索引，三个条件同时存在则查到对象，如果只有两个则查不到；
            cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        }
        //自定义异常
        if (cmsPage1!=null){
            this.update(cmsPage.getPageId(),cmsPage);
        }

        if (cmsPage1==null){
            cmsPage.setPageId(null);
            CmsPage save = cmsPageRepository.save(cmsPage);
            return new  CmsPageResult(CommonCode.SUCCESS,save);
        }
        return new  CmsPageResult(CommonCode.SUCCESS,cmsPage1) ;

    }
    //一键课程发布
    public  String publish(CmsPage cmsPage){
        CmsPageResult save = this.save(cmsPage);
        if (!save.isSuccess()){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        CmsPage cmsPage1 = save.getCmsPage();
        String pageId =cmsPage1.getPageId();
        String pageHtml = this.getPageHtml(pageId);
        if (StringUtils.isBlank(pageHtml)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //保存静态页面到GirdFS
        this.saveHtml(pageId,pageHtml);
        //使用rabbitMQ发送pageid
        this.sendMessage(pageId);
        //得到页面的url
        //页面url=站点域名+站点webpath+页面webpath+页面名称
        String siteId = cmsPage1.getSiteId();
        Optional<CmsSite> byId = cmsSiteRepository.findById(siteId);
        if (!byId.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        CmsSite cmsSite = byId.get();
        String urlPath=cmsSite.getSiteDomain()+cmsSite.getSiteWebPath()+cmsPage1.getPageWebPath()+cmsPage1.getPageName();
        return  urlPath;
    }
    //保存静态页面到GirdFS
    public CmsPage saveHtml(String pageId,String pageHtml){
        CmsPage query = this.query(pageId);
        if (query==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        try {
            InputStream inputStream = IOUtils.toInputStream(pageHtml, "utf-8");
            ObjectId objectId = gridFsTemplate.store(inputStream, query.getPageName());
            query.setHtmlFileId(objectId.toString());
            CmsPage save = cmsPageRepository.save(query);
            return save;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }
    //使用rabbitMQ发送pageid
    public void  sendMessage(String pageId){
        if (StringUtils.isBlank(pageId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        Map map = new HashMap();
        map.put("pageId",pageId);
        String s = JSON.toJSONString(map);
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM,"inform.#.email.#",s);

    }

}
