package com.xuecheng.framework.domain.ucenter.ext;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class AuthResult extends ResponseResult {
    String token ;
    public AuthResult(ResultCode resultCode, String token) {
        super(resultCode);
        this.token = token;
    }

}
