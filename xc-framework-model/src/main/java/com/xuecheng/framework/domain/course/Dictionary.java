package com.xuecheng.framework.domain.course;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@ToString
public class Dictionary {
    @Field("sd_name")
    private  String sdName;
    @Field("sd_id")
    private String sdId;
    @Field("sd_status")
    private String sdStatus;
}
