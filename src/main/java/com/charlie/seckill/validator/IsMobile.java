package com.charlie.seckill.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义校验注解
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class})
public @interface IsMobile {

    String message() default "手机号码格式错误";

    boolean required() default true;

    Class<?>[] groups() default {}; // 默认参数

    Class<? extends Payload>[] payload() default {};

}
