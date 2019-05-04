package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.response.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Value("${xuecheng.elasticsearch.index}")
    private String index;
    @Value("${xuecheng.elasticsearch.types}")
    private String types;
    @Value("${xuecheng.elasticsearch.source_field}")
    private String source_field;
    @Value("${xuecheng.media.index}")
    private String index_media;
    @Value("${xuecheng.media.types}")
    private String index_types;
    @Value("${xuecheng.media.source_field}")
    private String media_field;

    //搜索结果
    public QueryResponseResult<CoursePub> search(int page, int size, CourseSearchParam courseSearchParam) {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(types);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置过滤条件，查询到的字段包括什么，不包括什么
        String[] split = source_field.split(",");
        searchSourceBuilder.fetchSource(split,new String[]{});
        //设置分页查询
        if (page<=0){
            page=1;
        }
        if (size<=0){
            size=12;
        }
        int index = (page-1)*size;
        //从哪个索引开始
        searchSourceBuilder.from(index);
        //查几条数据
        searchSourceBuilder.size(size);
        //设置查询的条件全文检索
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //如何没有设置关键词查询 则查询所有
        if(StringUtils.isBlank(courseSearchParam.getKeyword())){
            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
            boolQueryBuilder.must(matchAllQueryBuilder);
        }else {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(),
                    "description","name").minimumShouldMatch("70%").field("name",10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //设置过滤条件
        if (StringUtils.isNotBlank(courseSearchParam.getMt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if (StringUtils.isNotBlank(courseSearchParam.getSt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if (StringUtils.isNotBlank(courseSearchParam.getGrade())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        List<CoursePub> list =null;
        long totalHits= 0;
        try {
            //执行查询
            SearchResponse search = restHighLevelClient.search(searchRequest);
            //查询到结果
            SearchHits hits = search.getHits();
            //查询到总条数
            totalHits = hits.getTotalHits();
            //如果没有查询到结果立刻返回失败
            if (totalHits==0){
                return new QueryResponseResult<CoursePub>(CommonCode.FAIL,null);
            }
            SearchHit[] hits1 = hits.getHits();
            list = new ArrayList<>();
            for (SearchHit hit:  hits1) {
                CoursePub coursePub = new CoursePub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出Id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                //取出名称
                String name = (String) sourceAsMap.get("name");
                coursePub.setName(name);
                //取出高亮名称
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields!=null){
                    HighlightField name1 = highlightFields.get("name");
                    if (name1!=null){
                        Text[] fragments = name1.getFragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for(Text tx :fragments){
                            stringBuffer.append(tx);
                        }
                        name =stringBuffer.toString();
                    }
                }
                //取出图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //取出价格

                Double price = (Double) sourceAsMap.get("price");
                if (price!=null){
                    coursePub.setPrice(price);}

                Double price_old = (Double) sourceAsMap.get("price_old");
                if (price_old!=null){
                    coursePub.setPrice_old(price_old);}

                list.add(coursePub);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        QueryResult<CoursePub>  queryResult = new QueryResult<>();
        queryResult.setList(list);
        queryResult.setTotal(totalHits);
        return  new QueryResponseResult<CoursePub>(CommonCode.SUCCESS,queryResult);
    }
    //视频扩展
    public Map<String, CoursePub> search(String id) {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(types);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置过滤条件，查询到的字段包括什么，不包括什么
        String[] split = source_field.split(",");
        searchSourceBuilder.fetchSource(split,new String[]{});
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            SearchHit[] hits1 = hits.getHits();
            Map<String, CoursePub> map = new HashMap<>();
            for (SearchHit hit :hits1) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                CoursePub coursePub = new CoursePub();
                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
               /* String teachplan = (String) sourceAsMap.get("teachplan");*/
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(courseId,coursePub);
            }
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }
    //根据课程teachplan——id 查询索引库  表xc_course_medic
    public TeachplanMediaPub seachTeachplanMediaPub(String teachplanId) {
        SearchRequest searchRequest = new SearchRequest(index_media);
        searchRequest.types(index_types);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置过滤条件，查询到的字段包括什么，不包括什么
        String[] split = media_field.split(",");
        searchSourceBuilder.fetchSource(split,new String[]{});
        searchSourceBuilder.query(QueryBuilders.termQuery("teachplan_id",teachplanId));
        SearchRequest source = searchRequest.source(searchSourceBuilder);
        SearchResponse search = null;
        try {
            search = restHighLevelClient.search(source);
            SearchHits hits = search.getHits();
            SearchHit[] hits1 = hits.getHits();
            TeachplanMediaPub teachplanMediaPub = new  TeachplanMediaPub();
            for (SearchHit hit :hits1){
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                teachplanMediaPub.setTeachplanId((String) sourceAsMap.get("teachplan_id"));
                teachplanMediaPub.setMediaId((String) sourceAsMap.get("media_id"));
                teachplanMediaPub.setMediaFileOriginalName((String) sourceAsMap.get("media_fileoriginalname"));
                teachplanMediaPub.setMediaUrl((String) sourceAsMap.get("media_url"));
                teachplanMediaPub.setCourseId((String) sourceAsMap.get("courseid"));
            }
            return teachplanMediaPub;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
