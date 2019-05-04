package com.xuecheng.framework.domain.course.ext;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.xuecheng.framework.domain.course.Dictionary;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@ToString
@Document(collection = "sys_dictionary")
public class SysDictionary {
    @Id
    private  String id;

    @Field("d_name")
    private  String dName;

    @Field("d_type")
    private  String dType;

    @Field("d_value")
    private List<Dictionary> dValue;

}