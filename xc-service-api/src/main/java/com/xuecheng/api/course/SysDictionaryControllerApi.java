package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.SysDictionary;

public interface SysDictionaryControllerApi {
    //课程等级，学习模式
    public SysDictionary findDictionaryByType(String type);

}
