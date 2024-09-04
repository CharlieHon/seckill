package com.charlie.seckill.vo;

import com.charlie.seckill.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * LoginVo：接收用户登陆时，发送的信息(mobile, password)
 * 
 * 通过注解修饰限制字段，当前端提交账号和密码时，如果不符合要求就会引发绑定异常-BindException
 */
@Data
public class LoginVo {
    // 对LoginVo的属性值进行约束
    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    @Length(min = 32)
    private String password;
}
