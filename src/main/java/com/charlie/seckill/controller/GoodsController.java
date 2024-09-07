package com.charlie.seckill.controller;

import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.GoodsService;
import com.charlie.seckill.service.UserService;
import com.charlie.seckill.vo.GoodsVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Resource
    private GoodsService goodsService;

    //// 进入商品列表页-将user对象保存到session中，session存入redis
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

    //// 进入商品列表页-2(从redis中直接获取user对象)
    //@RequestMapping("/toList")
    //public String toList(Model model, @CookieValue("userTicket") String ticket,
    //                     HttpServletRequest req, HttpServletResponse resp) {
    //
    //    // 如果cookie没有生成
    //    if (!StringUtils.hasText(ticket)) {
    //        return "login";
    //    }
    //
    //    // 从redis中获取user信息
    //    User user = userService.getUserByCookie(ticket, req, resp);
    //    if (user == null) {
    //        return "login";
    //    }
    //
    //    // 将user放到model，携带给下一个模板使用
    //    model.addAttribute("user", user);
    //
    //    return "goodsList";
    //}

    // 进入商品列表页3-获取浏览器传递的cookie值，进行参数解析，直接转成User对象，继续传递
    @RequestMapping("/toList")
    public String toList(Model model, User user) {

        if (user == null) {
            return "login";
        }

        // 将user用户信息放入到model，携带给下一个模板使用
        model.addAttribute("user", user);
        // 将商品列表信息，放入到
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }

    /**
     * 方法：进去到商品详情页面
     * @param user 通过自定义参数解析器处理返回的
     * @param goodsId 用户点击详情时，携带过来的
     */
    @RequestMapping("/toDetail/{goodsId}")
    public String toDetail(Model model, User user, @PathVariable("goodsId") Long goodsId) {
        if (null == user) { // 说明用户没有登录
            return "login";
        }
        model.addAttribute("user", user);

        // 通过goodsId，获取秒杀商品信息
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        // 将查询到的goodsVo放入到model，携带给下一个模板使用
        model.addAttribute("goods", goodsVo);
        return "goodsDetail";
    }
}
