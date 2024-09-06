package com.charlie.seckill.controller;

import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private UserService userService;

    //// 进入商品列表页
    //@RequestMapping("/toList")
    //public String toList(HttpSession session, Model model, @CookieValue("userTicket") String ticket) {
    //
    //    // 如果cookie没有生成
    //    if (!StringUtils.hasText(ticket)) {
    //        return "login";
    //    }
    //
    //    // 通过ticket获取session中存放的user
    //    User user = (User) session.getAttribute(ticket);
    //    if (Objects.isNull(user)) { // 用户没有成功登录
    //        return "login";
    //    }
    //
    //    // 将user放到model，携带给下一个模板使用
    //    model.addAttribute("user", user);
    //
    //    return "goodsList";
    //}

    // 进入商品列表页-2
    @RequestMapping("/toList")
    public String toList(Model model, @CookieValue("userTicket") String ticket,
                         HttpServletRequest req, HttpServletResponse resp) {

        // 如果cookie没有生成
        if (!StringUtils.hasText(ticket)) {
            return "login";
        }

        // 从redis中获取user信息
        User user = userService.getUserByCookie(ticket, req, resp);
        if (user == null) {
            return "login";
        }

        // 将user放到model，携带给下一个模板使用
        model.addAttribute("user", user);

        return "goodsList";
    }

}
