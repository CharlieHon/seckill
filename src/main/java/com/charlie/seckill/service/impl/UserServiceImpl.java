package com.charlie.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charlie.seckill.exception.GlobalException;
import com.charlie.seckill.mapper.UserMapper;
import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.UserService;
import com.charlie.seckill.util.MD5Util;
import com.charlie.seckill.util.ValidatorUtil;
import com.charlie.seckill.vo.LoginVo;
import com.charlie.seckill.vo.RespBean;
import com.charlie.seckill.vo.RespBeanEnum;
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

        return RespBean.success();
    }
}
