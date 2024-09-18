package com.charlie.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charlie.seckill.exception.GlobalException;
import com.charlie.seckill.mapper.UserMapper;
import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.UserService;
import com.charlie.seckill.util.CookieUtil;
import com.charlie.seckill.util.MD5Util;
import com.charlie.seckill.util.UUIDUtil;
import com.charlie.seckill.util.ValidatorUtil;
import com.charlie.seckill.vo.LoginVo;
import com.charlie.seckill.vo.RespBean;
import com.charlie.seckill.vo.RespBeanEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Objects;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    // 配置RedisTemplate，操作redis
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest req, HttpServletResponse resp) {

        // 接收mobile和password(midPass)
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        //// 判断手机号/id，和密码是否为空   --- NotNull
        //if (!StringUtils.hasText(mobile) || !StringUtils.hasText(password)) {
        //    return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        //}
        //
        //// 校验手机号码是否合格           --- IsMobile
        //if (!ValidatorUtil.isMobile(mobile)) {
        //    return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        //}

        // 查询DB，看看用户是否存在
        User user = userMapper.selectById(mobile);
        if (Objects.isNull(user)) {
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            // 通过GlobalException处理
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        // 如果用户存在，则比对密码
        // 注意：从loginVo取出的密码是中间加密加盐的
        if (!MD5Util.midPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }

        // 用户登录成功，给每个用户生成ticket-唯一
        String ticket = UUIDUtil.uuid();

        // 原(保存session)：将登录成功的用户保存到session(服务器)，引入spring-session-data-redis后自动进行
        //req.getSession().setAttribute(ticket, user);

        // 现(保存user)：为了实现分布式session，把登录的用户存放到redis
        redisTemplate.opsForValue().set("user:" + ticket, user);

        // 将ticket保存到cookie(浏览器)
        CookieUtil.setCookie(req, resp, "userTicket", ticket);

        // 返回生成的票据userTicket
        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest req, HttpServletResponse resp) {

        if (!StringUtils.hasText(userTicket)) {
            return null;
        }

        // 根据userTicket到redis获取user
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        // 如果用户不为null，就重新设置cookie，刷新（防止过期等，根据业务需求来）
        if (user != null) {
            CookieUtil.setCookie(req, resp, "userTicket", userTicket);
        }

        return user;
    }

    // 更新用户的密码
    @Override
    public RespBean updatePassword(String userTicket, String password, HttpServletRequest req, HttpServletResponse resp) {
        User user = getUserByCookie(userTicket, req, resp);
        if (user == null) {
            // 抛出异常
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }

        // 设置新密码
        user.setPassword(MD5Util.inputPassToDBPass(password, user.getSalt()));
        // 更新数据库
        int i = userMapper.updateById(user);
        if (i == 1) {   // 更新成功
            // 删除该用户再redis中的数据
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }
        // 密码更新失败
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
