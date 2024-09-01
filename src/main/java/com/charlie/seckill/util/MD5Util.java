package com.charlie.seckill.util;


import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5Utils：工具类，根据密码设计方案提供响应的方法
 */
public class MD5Util {

    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    // 准备一个salt
    private static final String SALT = "UCmP7xHA";

    // 加密加盐，md5(password明文+salt)
    public static String inputPassToMidPass(String inputPass) {
        // 加盐
        String str = SALT.charAt(0) + inputPass + SALT.charAt(6);
        // 加密
        return md5(str);
    }

    // 加密加盐（第二次），md5(md5(password明文+salt) + salt2)
    public static String midPassToDBPass(String midPass, String salt) {
        String str = salt.charAt(1) + midPass + salt.charAt(5);
        return md5(str);
    }

    // 将password明文，直接转成DB中的密码
    public static String inputPassToDBPass(String inputPass, String salt) {
        String midPass = inputPassToMidPass(inputPass);
        String dbPass = midPassToDBPass(midPass, salt);
        return dbPass;
    }
}
