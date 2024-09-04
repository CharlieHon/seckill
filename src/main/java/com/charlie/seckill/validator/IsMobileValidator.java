package com.charlie.seckill.validator;

import com.charlie.seckill.util.ValidatorUtil;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 自定义校验器
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {
    private boolean required = false;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (required) { // 必填
            return ValidatorUtil.isMobile(value);
        } else {        // 非必填
            if (!StringUtils.hasText(value)) {
                return true;
            } else {
                return ValidatorUtil.isMobile(value);
            }
        }
    }

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        // 初始化
        required = constraintAnnotation.required();
    }
}
