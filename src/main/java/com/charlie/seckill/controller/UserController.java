package com.charlie.seckill.controller;

import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.UserService;
import com.charlie.seckill.vo.RespBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 方法：返回登录用户的信息，同时接收请求参数address
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user) {
        return RespBean.success(user);
    }

    // 处理更新密码请求
    @RequestMapping("/updpwd")
    @ResponseBody
    public RespBean updatePassword(String userTicket, String password, HttpServletRequest req, HttpServletResponse resp) {
        return userService.updatePassword(userTicket, password, req, resp);
    }
}
