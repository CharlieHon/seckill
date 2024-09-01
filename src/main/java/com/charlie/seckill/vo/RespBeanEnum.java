package com.charlie.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * RespBeanEnum：返回信息的枚举类
 */

@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {

    // 通用
    SUCCESS(200, "success"),
    ERROR(500, "服务端异常"),

    // 登录
    LOGIN_ERROR(500210, "用户id或者密码错误"),
    MOBILE_ERROR(500211, "手机号码格式不正确"),
    MOBILE_NOT_EXIST(500213, "手机号码不存在");
    // 其它功能在需要时开发

    private final Integer code;
    private final String message;

}
