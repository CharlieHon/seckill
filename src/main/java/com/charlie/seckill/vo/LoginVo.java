package com.charlie.seckill.vo;

import lombok.Data;

/**
 * LoginVo：接收用户登陆时，发送的信息(mobile, password)
 */
@Data
public class LoginVo {
    private String mobile;
    private String password;
}
