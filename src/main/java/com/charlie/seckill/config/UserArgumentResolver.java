package com.charlie.seckill.config;

import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.UserService;
import com.charlie.seckill.util.CookieUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User类型参数解析器
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Resource
    private UserService userService;

    // 判断当前要解析的参数类型，是不是目标类型
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> aClass = parameter.getParameterType();
        return aClass == User.class;
    }

    // 如果 supportsParameter 返回true，就执行下面的解析工作，根据业务进行编写
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse resp = webRequest.getNativeResponse(HttpServletResponse.class);

        String ticket = CookieUtil.getCookieValue(req, "userTicket");
        if (!StringUtils.hasText(ticket)) {
            return null;
        }

        // 从redis获取用户，处理后的user传给 public String toList(Model model, User user);
        return userService.getUserByCookie(ticket, req, resp);
    }
}
