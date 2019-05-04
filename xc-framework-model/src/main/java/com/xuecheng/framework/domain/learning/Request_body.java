package com.xuecheng.framework.domain.learning;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Request_body {
    private String userId;
    private String courseId;
}
