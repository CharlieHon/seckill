package com.charlie.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespBean {

    private long code;
    private String message;
    private Object obj;

    // 成功后，同时携带数据
    public static RespBean success(Object data) {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), data);
    }

    // 成功后，不携带数据
    public static RespBean success() {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), null);
    }

    // 失败各有不同，返回失败信息时不携带数据
    public static RespBean error(RespBeanEnum respBeanEnum) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), null);
    }

    // 失败时返回失败信息，同时返回数据
    public static RespBean error(RespBeanEnum respBeanEnum, Object data) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), data);
    }

}
