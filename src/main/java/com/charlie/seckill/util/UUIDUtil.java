package com.charlie.seckill.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * 生成UUID的工具类
 */
public class UUIDUtil {

    public static String uuid() {
        // 默认下生成的字符串形式 xxx-yyy-zzz，去除其中的-
        return UUID.randomUUID().toString().replace("-", "");
    }

}
