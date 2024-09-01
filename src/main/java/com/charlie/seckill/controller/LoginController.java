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

    // 编写方法，处理用户登录请求；ResponseBody表示返回json数据而非进行页面跳转
    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest req, HttpServletResponse resp) {
        log.info("{}", loginVo);
        return userService.doLogin(loginVo, req, resp);
    }

}
