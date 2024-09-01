package com.charlie.seckill.util;

import org.junit.jupiter.api.Test;
import sun.security.provider.MD5;

/**
 * 测试 MD5Util 方法的使用
 */
public class MD5UtilTest {

    private static final String SRC = "12346";

    // 中间密码二次加密加盐时的salt
    private static final String SALT = "cLo8QmTG";

    @Test
    public void f1() {
        // 密码明文 "123456"
        // 1. 获取到密码明文 "123456" 的中间密码，即客户端加密加盐，在网络上传输的密码
        String midPass = MD5Util.inputPassToMidPass(SRC);
        System.out.println("midPass=" + midPass);
        // 2. 第二次加密加盐，即存放在DB中的密码
        String dbPass = MD5Util.midPassToDBPass(midPass, SALT);
        System.out.println("dbPass=" + dbPass);
        // 3. 由明文直接得到数据库中密码
        String dbPass2 = MD5Util.inputPassToDBPass(SRC, SALT);
        System.out.println("dbPass2=" + dbPass2);
    }

}
