package com.charlie.seckill.controller;

import com.charlie.seckill.pojo.Order;
import com.charlie.seckill.pojo.SeckillOrder;
import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.GoodsService;
import com.charlie.seckill.service.OrderService;
import com.charlie.seckill.service.SeckillOrderService;
import com.charlie.seckill.vo.GoodsVo;
import com.charlie.seckill.vo.RespBeanEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    // 装配需要的组件/对象
    @Resource
    private GoodsService goodsService;

    @Resource
    private SeckillOrderService seckillOrderService;

    @Resource
    private OrderService orderService;

    @Resource
    private RedisTemplate redisTemplate;

    ///**
    // * 处理用户秒杀/抢购请求
    // *
    // * @param model   携带数据到下一个页面
    // * @param user    用户
    // * @param goodsId 秒杀商品id
    // * @return 秒杀结果页面
    // * V1.0基础版本，在高并发情况下，还要再做优化
    // */
    //@RequestMapping("/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
    //
    //    System.out.println("------------秒杀V1.0------------");
    //
    //    // 用户没有登录
    //    if (user == null) {
    //        return "login";
    //    }
    //
    //    // 将user放入model，下一个模板可以使用
    //    model.addAttribute("user", user);
    //
    //    // 获取到goodsVo
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    // 判断库存
    //    if (goodsVo.getStockCount() < 1) {  // 没有库存
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";   // 错误页面
    //    }
    //
    //    // 判断用户是否是复购，判断当前购买用户id和购买商品id是否已经在商品秒杀表存在
    //    // user_id and goods_id 同时相同，因为一个用户可以购买多个秒杀商品
    //    SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
    //    if (seckillOrder != null) { // 已经存在，不能复购
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
    //        return "secKillFail";   // 错误页面
    //    }
    //
    //    // 可以进行抢购
    //    Order order = orderService.seckill(user, goodsVo);
    //    if (order == null) {
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";   // 错误页面
    //    }
    //
    //    // 秒杀成功，进入到订单页
    //    model.addAttribute("order", order);
    //    model.addAttribute("goods", goodsVo);
    //
    //    // System.out.println("------------秒杀V2.0------------");
    //
    //    // 进入到订单详情页
    //    return "orderDetail";
    //}

    /**
     * 处理用户秒杀/抢购请求
     *
     * @param model   携带数据到下一个页面
     * @param user    用户
     * @param goodsId 秒杀商品id
     * @return 秒杀结果页面
     * V2.0改进版
     */
    @RequestMapping("/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId) {

        // 用户没有登录
        if (user == null) {
            return "login";
        }

        // 将user放入model，下一个模板可以使用
        model.addAttribute("user", user);

        // 获取到goodsVo
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        // 判断库存
        if (goodsVo.getStockCount() < 1) {  // 没有库存
            model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
            return "secKillFail";   // 错误页面
        }

        // 解决复购问题：判断用户是否是复购，直接到redis获取对应的秒杀订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (null != seckillOrder) { // 不为null，则说明该用户已经抢购该商品，则返回错误页面
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
            return "secKillFail";
        }

        // 可以进行抢购
        Order order = orderService.seckill(user, goodsVo);
        if (order == null) {
            model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
            return "secKillFail";   // 错误页面
        }

        // 秒杀成功，进入到订单页
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);

        // System.out.println("------------秒杀V2.0------------");

        // 进入到订单详情页
        return "orderDetail";
    }
}
