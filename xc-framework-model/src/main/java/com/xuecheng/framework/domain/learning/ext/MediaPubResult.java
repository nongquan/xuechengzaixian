package com.xuecheng.framework.domain.learning.ext;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class MediaPubResult extends ResponseResult {
    String mediaUrl ;
    public MediaPubResult(ResultCode resultCode, String mediaUrl) {
        super(resultCode);
        this.mediaUrl = mediaUrl;
    }
}
