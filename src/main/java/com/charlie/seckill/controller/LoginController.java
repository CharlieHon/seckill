package com.charlie.seckill.controller;

import com.charlie.seckill.service.UserService;
import com.charlie.seckill.vo.LoginVo;
import com.charlie.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    // 装配UserService
    @Resource
    private UserService userService;

    // 编写方法，可以进入登录页面
    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login"; // 到templates/login.html
    }

    /**
     * 编写方法，处理用户登录请求；ResponseBody表示返回json数据而非进行页面跳转
     *
     * 因为是通过controller接收前端请求参数的，所以在此添加 @Valid 注解来对参数进行校验(@NotNull @IsMobile)
     */
    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest req, HttpServletResponse resp) {
        //log.info("{}", loginVo);
        return userService.doLogin(loginVo, req, resp);
    }

}
