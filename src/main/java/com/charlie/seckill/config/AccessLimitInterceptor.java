package com.charlie.seckill.config;

import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.UserService;
import com.charlie.seckill.util.CookieUtil;
import com.charlie.seckill.vo.RespBean;
import com.charlie.seckill.vo.RespBeanEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * 自定义拦截器
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    // 装配需要的组件/对象
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    // 在目标方法执行之前被调用，返回false则不会再执行目标方法，可以在此将响应请求返回给页面；返回true表示放行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.得到user对象，并放入到ThreadLocal
        if (handler instanceof HandlerMethod) {
            // 获取到登录的user对象
            User user = getUser(request, response);
            // 存入到ThreadLocal
            UserContext.setUser(user);

            // 2. 处理 @AccessLimit 限流防刷注解
            // 把handler转成HandlerMethod
            HandlerMethod hm =  (HandlerMethod) handler;
            // 获取到目标方法的注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {  // 目标方法没有 @AccessLimit ，说明该接口并没有处理限流防刷
                return true;
            }
            // 获取注解的属性值
            int second = accessLimit.second();  // 获取到时间范围
            int maxCount = accessLimit.maxCount();  // 获取到时间返回
            boolean needLogin = accessLimit.needLogin();
            if (needLogin) {    // 说明用户必须登录，才能访问该方法/接口
                if (user == null) { // 没有登录，请求被拦截
                    render(response, RespBeanEnum.SESSION_ERROR);
                    return false;
                }
            }
            // 限流逻辑
            String uri = request.getRequestURI();
            String key = uri + ":" + user.getId();
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Integer count = (Integer) valueOperations.get(key);
            if (count == null) {    // 如果还没有key，就初始化值为1，并这是指定过期时间
                valueOperations.set(key, 1, second, TimeUnit.SECONDS);
            } else if (count < maxCount) {
                valueOperations.increment(key);
            } else {    // 用户频繁访问，拦截请求
                render(response, RespBeanEnum.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    // 方法：构建返回对象，以流的形式返回
    private void render(HttpServletResponse resp, RespBeanEnum respBeanEnum) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        // 构建RespBean
        RespBean error = RespBean.error(respBeanEnum);
        out.write(new ObjectMapper().writeValueAsString(error));
        out.flush();
        out.close();
    }

    // 方法：得到登录的对象，依赖携带的userTicket
    private User getUser(HttpServletRequest req, HttpServletResponse resp) {
        String userTicket = CookieUtil.getCookieValue(req, "userTicket");
        if (!StringUtils.hasText(userTicket)) {
            return null;    // 说明该用户没有登录，直接放回null
        }
        return userService.getUserByCookie(userTicket, req, resp);
    }

}
