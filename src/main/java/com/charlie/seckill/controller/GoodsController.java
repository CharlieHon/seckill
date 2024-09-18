package com.charlie.seckill.controller;

import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.GoodsService;
import com.charlie.seckill.service.UserService;
import com.charlie.seckill.vo.GoodsVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private UserService userService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private RedisTemplate redisTemplate;

    // 手动进行渲染，需要的模板解析器
    @Resource
    private ThymeleafViewResolver thymeleafViewResolver;

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

    //// 进入商品列表页3-获取浏览器传递的cookie值，进行参数解析，直接转成User对象，继续传递
    //@RequestMapping("/toList")
    //public String toList(Model model, User user) {
    //
    //    if (user == null) {
    //        return "login";
    //    }
    //
    //    // 将user用户信息放入到model，携带给下一个模板使用
    //    model.addAttribute("user", user);
    //    // 将商品列表信息，放入到
    //    model.addAttribute("goodsList", goodsService.findGoodsVo());
    //    return "goodsList";
    //}

    // 进入商品列表4-使用redis缓存优化
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest req, HttpServletResponse resp) {
        if (null == user) {
            return "login";
        }

        // 先到redis获取页面，如果有，直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if (StringUtils.hasText(html)) {
            return html;
        }

        // 将user用户信息放入到model
        model.addAttribute("user", user);
        // 如果redis缓存中不存在数据，则向数据库中查询
        model.addAttribute("goodsList", goodsService.findGoodsVo());

        // 如果从redis中没有获取到页面，就手动渲染页面，并存入到redis。webContext上下文环境
        WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if (StringUtils.hasText(html)) {
            // 渲染成功，将页面保存到redis。设置为每60s更新一次，即该页面60s失效，redis会清楚该页面
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    ///**
    // * 方法：进去到商品详情页面(每次都从DB查询)
    // * @param user 通过自定义参数解析器处理返回的
    // * @param goodsId 用户点击详情时，携带过来的
    // */
    //@RequestMapping("/toDetail/{goodsId}")
    //public String toDetail(Model model, User user, @PathVariable("goodsId") Long goodsId) {
    //    if (null == user) { // 说明用户没有登录
    //        return "login";
    //    }
    //    model.addAttribute("user", user);
    //
    //    // 通过goodsId，获取秒杀商品信息
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //    // 将查询到的goodsVo放入到model，携带给下一个模板使用
    //    model.addAttribute("goods", goodsVo);
    //
    //    // 说明：返回秒杀商品详情时，同时返回该商品的秒杀状态和秒杀的剩余遇见
    //    // 为了配合前端展示秒杀商品的状态
    //    // 1. 变量 secKillStatus 秒杀状态 0：秒杀未开始，1：秒杀进行中，2：秒杀已经结束
    //    // 2. 秒杀 remainSeconds 剩余秒数 > 0 表示还有多久开始秒杀，0：秒杀进行中，-1：秒杀已经结束
    //    Date startDate = goodsVo.getStartDate();    // 秒杀开始时间
    //    Date endDate = goodsVo.getEndDate();        // 秒杀结束时间
    //    Date nowDate = new Date();                  // 当前时间
    //
    //    int secKillStatus = 0;  // 秒杀状态
    //    int remainSeconds = 0;  // 秒杀剩余时间
    //
    //    // 如果nowDate在startDate之前，说明还没有开始秒杀
    //    if (nowDate.before(startDate)) {
    //        // 得到还有多少秒开始秒杀
    //       remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
    //    } else if (nowDate.after(endDate)) {    // 秒杀已经结束
    //        secKillStatus = 2;
    //        remainSeconds = -1;
    //    } else {    // 秒杀进行中
    //        secKillStatus = 1;
    //    }
    //
    //    // 将secKillStatus和remainSeconds放入到model，携带到模板页使用
    //    model.addAttribute("secKillStatus", secKillStatus);
    //    model.addAttribute("remainSeconds", remainSeconds);
    //
    //    return "goodsDetail";
    //}

    /**
     * 方法：进去到商品详情页面(使用redis进行优化)
     *
     * @param user    通过自定义参数解析器处理返回的
     * @param goodsId 用户点击详情时，携带过来的
     */
    @ResponseBody
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    public String toDetail(Model model, User user, @PathVariable("goodsId") Long goodsId,
                           HttpServletRequest req, HttpServletResponse resp) {
        if (null == user) { // 说明用户没有登录
            return "login";
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if (StringUtils.hasText(html)) {
            return html;
        }

        model.addAttribute("user", user);
        // 通过goodsId，获取秒杀商品信息
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        // 将查询到的goodsVo放入到model，携带给下一个模板使用
        model.addAttribute("goods", goodsVo);

        // 说明：返回秒杀商品详情时，同时返回该商品的秒杀状态和秒杀的剩余遇见
        // 为了配合前端展示秒杀商品的状态
        // 1. 变量 secKillStatus 秒杀状态 0：秒杀未开始，1：秒杀进行中，2：秒杀已经结束
        // 2. 秒杀 remainSeconds 剩余秒数 > 0 表示还有多久开始秒杀，0：秒杀进行中，-1：秒杀已经结束
        Date startDate = goodsVo.getStartDate();    // 秒杀开始时间
        Date endDate = goodsVo.getEndDate();        // 秒杀结束时间
        Date nowDate = new Date();                  // 当前时间

        int secKillStatus = 0;  // 秒杀状态
        int remainSeconds = 0;  // 秒杀剩余时间

        // 如果nowDate在startDate之前，说明还没有开始秒杀
        if (nowDate.before(startDate)) {
            // 得到还有多少秒开始秒杀
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)) {    // 秒杀已经结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {    // 秒杀进行中
            secKillStatus = 1;
        }

        // 将secKillStatus和remainSeconds放入到model，携带到模板页使用
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        // redis中没有缓存，则进行手动页面渲染
        WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if (StringUtils.hasText(html)) {
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
    }
}
