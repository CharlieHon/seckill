package com.charlie.seckill.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.charlie.seckill.pojo.User;
import com.charlie.seckill.vo.LoginVo;
import com.charlie.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService extends IService<User> {

    // 完成用户的登录校验
    RespBean doLogin(LoginVo loginVo, HttpServletRequest req, HttpServletResponse resp);

    // 通过cookie值到redis获取用户信息
    User getUserByCookie(String userTicket, HttpServletRequest req, HttpServletResponse resp);

    // 方法：更新密码
    RespBean updatePassword(String userTicket, String password, HttpServletRequest req, HttpServletResponse resp);
}
