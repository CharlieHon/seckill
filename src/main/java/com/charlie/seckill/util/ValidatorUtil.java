package com.charlie.seckill.util;

import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ValidatorUtil：校验类，完成一些校验工作，比如手机号码格式是否正确
 * 提示：正则表达式
 */
public class ValidatorUtil {

    // 校验手机号码的正则表达式
    // 13300000000 合格
    // 11000000000 不合格
    private static final Pattern mobile_pattern = Pattern.compile("^1[3-9][0-9]{9}$");

    // 编写方法，如果电话格式满足规则，返回true；否则，返回false
    public static boolean isMobile(String mobile) {
        if (!StringUtils.hasText(mobile)) {
            return false;
        }

        // 进行正则表达式校验
        Matcher matcher = mobile_pattern.matcher(mobile);
        return matcher.matches();
    }

    // 校验测试
    @Test
    public void t1() {
        String mobile = "13300000000";
        System.out.println(isMobile(mobile));
    }

}
