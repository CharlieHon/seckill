package com.charlie.seckill.controller;

import cn.hutool.json.JSONUtil;
import com.charlie.seckill.pojo.Order;
import com.charlie.seckill.pojo.SeckillMessage;
import com.charlie.seckill.pojo.SeckillOrder;
import com.charlie.seckill.pojo.User;
import com.charlie.seckill.rabbitmq.MQSenderMessage;
import com.charlie.seckill.service.GoodsService;
import com.charlie.seckill.service.OrderService;
import com.charlie.seckill.service.SeckillOrderService;
import com.charlie.seckill.vo.GoodsVo;
import com.charlie.seckill.vo.RespBean;
import com.charlie.seckill.vo.RespBeanEnum;
import com.ramostear.captcha.HappyCaptcha;
import com.ramostear.captcha.common.Fonts;
import com.ramostear.captcha.support.CaptchaStyle;
import com.ramostear.captcha.support.CaptchaType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    // 装配需要的组件/对象
    @Resource
    private GoodsService goodsService;

    @Resource
    private SeckillOrderService seckillOrderService;

    @Resource
    private OrderService orderService;

    @Resource
    private RedisTemplate redisTemplate;

    // 装配消息的生产者/发送者
    @Resource
    private MQSenderMessage mqSenderMessage;

    // 定义map，记录秒杀商品是否还有库存
    private HashMap<Long, Boolean> entryStockMap = new HashMap<>();

    /**
     * 该方法在类(SeckillController)的所有属性都初始化后，自动执行
     * 在这里完成将所有商品的库存量，加载到redis
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 查询所有的秒杀商品
        List<GoodsVo> list = goodsService.findGoodsVo();
        // 先判断是否为空
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        // 遍历list，然后将秒杀商品的库存量放入到redis
        // 秒杀商品库存量对应key-> seckillGoods:商品id
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            // 初始化map，如果goodsId: false表示有库存
            entryStockMap.put(goodsVo.getId(), false);
        });
    }

    // 生成验证码-happyCaptcha
    @RequestMapping("/captcha")
    public void happyCaptcha(User user, Long goodsId, HttpServletRequest req, HttpServletResponse resp) {
        // 生成验证码，并输出。验证码默认保存到session中，key是happy-captcha
        HappyCaptcha.require(req, resp)
                .style(CaptchaStyle.ANIM)   // 设置样式为动画
                .type(CaptchaType.NUMBER)   // 设置验证码内容为数字
                .length(6)                  // 设置字符长度为6
                .width(220)                 // 设置动画宽度为220
                .height(80)                 // 设置动画高度为80
                .font(Fonts.getInstance().zhFont()) // 设置汉字的字体
                .build().finish();          // 生成并输出验证码
        // 把验证码的值，保存到redis[考虑项目分布式]，key- captcha:userId:goodsId，验证码失效时间30s
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId,
                req.getSession().getAttribute("happy-captcha"), 30, TimeUnit.SECONDS);
    }


    // 获取秒杀路径，与验证码校验
    @ResponseBody
    @RequestMapping("/path")
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest req) {
        if (user == null || goodsId < 0 || !StringUtils.hasText(captcha)) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        // PRO: 秒杀接口限流防刷，加入Redis计数器，完成对用户的限流防刷
        //  比如：在5s内访问次数超过了5次，则认为是在刷接口
        // uri就是 localhost:8080/seckill/path的 /seckill/path
        String uri = req.getRequestURI();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = uri + ":" + user.getId();
        Integer count = (Integer) valueOperations.get(key);
        if (count == null) {    // 说明还没有该key，即第一次访问。设置访问次数为1，超时时间为5秒
            valueOperations.set(key, 1, 5, TimeUnit.SECONDS);
        } else if (count < 5) { // 说明正常访问
            valueOperations.increment(key);
        } else {    // 说明用户在5秒内频繁访问，次数>=5
            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
        }

        // PRO: 增加业务逻辑，校验用户输入的验证码是否正确
        boolean checkCaptcha = orderService.checkCaptcha(user, goodsId, captcha);
        if (!checkCaptcha) {    // 校验失败
            return RespBean.error(RespBeanEnum.CAPTCHA_ERROR);
        }

        String path = orderService.createPath(user, goodsId);
        return RespBean.success(path);
    }

    /**
     * 处理用户秒杀/抢购请求
     *
     * @param model   携带数据到下一个页面
     * @param user    用户
     * @param goodsId 秒杀商品id
     * @return 秒杀结果页面
     * V6.0 改进版，加入秒杀地址校验，直接返回RespBean
     */
    @ResponseBody
    @RequestMapping("/{path}/doSeckill")
    public RespBean doSeckill(Model model, User user, Long goodsId, @PathVariable String path) {

        // 用户没有登录
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        // PRO[V6.0]: 加入判断逻辑，判断用户携带路径是否正确
        boolean b = orderService.checkPath(user, goodsId, path);
        if (!b) {   // 校验失败，请求非法
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }

        // 获取到goodsVo
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        // 判断库存
        if (goodsVo.getStockCount() < 1) {  // 没有库存
            model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
            return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        }

        // 解决复购问题：判断用户是否是复购，直接到redis获取对应的秒杀订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (null != seckillOrder) { // 不为null，则说明该用户已经抢购该商品，则返回错误页面
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }

        // PRO[4.0]: 对map进行判断[内存标记]，如果商品在map中已经没有库存，直接返回，无需进行redis预减
        if (entryStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        }

        // PRO[3.0]: [!!!]库存预减需要放在复购之后，因为预减库存成功即意味着就要到db中去修改数据。如果预减成功，但是发现复购，
        //  方法返回而没有实际到db更新数据，就会导致redis库存量与db实际库存量不匹配，可能导致库存余留

        // PRO[3.0]: 库存预减，如果在redis中预减库存，发现秒杀商品已经没有了，就直接返回。
        //  从而减少执行orderService.seckill()请求，防止线成堆积，优化秒杀/高并发
        //  decrement()方法具有原子性[!!!]，返回减一后的结果
        Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
        if (decrement < 0) {
            // 说明当前商品已经没有库存
            entryStockMap.put(goodsId, true);

            // 恢复库存为0
            redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        }

        // PRO[V5.0]: 抢购，向消息队列发送秒杀请求，实现了秒杀的异步请求。
        //  发送秒杀消息后，立即快速返回结束[临时结果]，比如“排队中”。
        //  客户端可以通过轮询，获取到最终结果
        // 创建SeckillMessage对象
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSenderMessage.sendSeckillMessage(JSONUtil.toJsonStr(seckillMessage));
        return RespBean.error(RespBeanEnum.SEC_KILL_WAIT);
    }

    ///**
    // * 处理用户秒杀/抢购请求
    // *
    // * @param model   携带数据到下一个页面
    // * @param user    用户
    // * @param goodsId 秒杀商品id
    // * @return 秒杀结果页面
    // * V5.0 改进版，使用消息队列，实现秒杀的异步请求
    // */
    //@RequestMapping("/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
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
    //    // 解决复购问题：判断用户是否是复购，直接到redis获取对应的秒杀订单
    //    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
    //    if (null != seckillOrder) { // 不为null，则说明该用户已经抢购该商品，则返回错误页面
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
    //        return "secKillFail";
    //    }
    //
    //    // PRO[4.0]: 对map进行判断[内存标记]，如果商品在map中已经没有库存，直接返回，无需进行redis预减
    //    if (entryStockMap.get(goodsId)) {
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";
    //    }
    //
    //    // PRO[3.0]: [!!!]库存预减需要放在复购之后，因为预减库存成功即意味着就要到db中去修改数据。如果预减成功，但是发现复购，
    //    //  方法返回而没有实际到db更新数据，就会导致redis库存量与db实际库存量不匹配，可能导致库存余留
    //
    //    // PRO[3.0]: 库存预减，如果在redis中预减库存，发现秒杀商品已经没有了，就直接返回。
    //    //  从而减少执行orderService.seckill()请求，防止线成堆积，优化秒杀/高并发
    //    //  decrement()方法具有原子性[!!!]，返回减一后的结果
    //    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
    //    if (decrement < 0) {
    //        // 说明当前商品已经没有库存
    //        entryStockMap.put(goodsId, true);
    //
    //        // 恢复库存为0
    //        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";
    //    }
    //
    //    // PRO[V5.0]: 抢购，向消息队列发送秒杀请求，实现了秒杀的异步请求。
    //    //  发送秒杀消息后，立即快速返回结束[临时结果]，比如“排队中”。
    //    //  客户端可以通过轮询，获取到最终结果
    //    // 创建SeckillMessage对象
    //    SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
    //    mqSenderMessage.sendSeckillMessage(JSONUtil.toJsonStr(seckillMessage));
    //    model.addAttribute("errmsg", "排队中...");
    //    return "secKillFail";
    //}

    ///**
    // * 处理用户秒杀/抢购请求
    // *
    // * @param model   携带数据到下一个页面
    // * @param user    用户
    // * @param goodsId 秒杀商品id
    // * @return 秒杀结果页面
    // * V4.0 改进版，使用Redis预减库存，减少到db中更新数据的次数。加入内存标记优化，减少redis重复操作
    // */
    //@RequestMapping("/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
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
    //    // 解决复购问题：判断用户是否是复购，直接到redis获取对应的秒杀订单
    //    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
    //    if (null != seckillOrder) { // 不为null，则说明该用户已经抢购该商品，则返回错误页面
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
    //        return "secKillFail";
    //    }
    //
    //    // PRO: 对map进行判断[内存标记]，如果商品在map中已经没有库存，直接返回，无需进行redis预减
    //    if (entryStockMap.get(goodsId)) {
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";
    //    }
    //
    //    // PRO: [!!!]库存预减需要放在复购之后，因为预减库存成功即意味着就要到db中去修改数据。如果预减成功，但是发现复购，
    //    //  方法返回而没有实际到db更新数据，就会导致redis库存量与db实际库存量不匹配，可能导致库存余留
    //
    //    // PRO: 库存预减，如果在redis中预减库存，发现秒杀商品已经没有了，就直接返回。
    //    //  从而减少执行orderService.seckill()请求，防止线成堆积，优化秒杀/高并发
    //    //  decrement()方法具有原子性[!!!]，返回减一后的结果
    //    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
    //    if (decrement < 0) {
    //        // 说明当前商品已经没有库存
    //        entryStockMap.put(goodsId, true);
    //
    //        // 恢复库存为0
    //        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";
    //    }
    //
    //    // 抢购
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
    //    // 进入到订单详情页
    //    return "orderDetail";
    //}

    ///**
    // * 处理用户秒杀/抢购请求
    // *
    // * @param model   携带数据到下一个页面
    // * @param user    用户
    // * @param goodsId 秒杀商品id
    // * @return 秒杀结果页面
    // * V3.0 改进版，使用Redis预减库存，减少到db中更新数据的次数
    // */
    //@RequestMapping("/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
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
    //    // 解决复购问题：判断用户是否是复购，直接到redis获取对应的秒杀订单
    //    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
    //    if (null != seckillOrder) { // 不为null，则说明该用户已经抢购该商品，则返回错误页面
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
    //        return "secKillFail";
    //    }
    //
    //    // PRO: [!!!]库存预减需要放在复购之后，因为预减库存成功即意味着就要到db中去修改数据。如果预减成功，但是发现复购，
    //    //  方法返回而没有实际到db更新数据，就会导致redis库存量与db实际库存量不匹配，可能导致库存余留
    //
    //    // PRO: 库存预减，如果在redis中预减库存，发现秒杀商品已经没有了，就直接返回。
    //    //  从而减少执行orderService.seckill()请求，防止线成堆积，优化秒杀/高并发
    //    //  decrement()方法具有原子性[!!!]，返回减一后的结果
    //    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
    //    if (decrement < 0) {    // 说明当前商品已经没有库存
    //        // 恢复库存为0
    //        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";
    //    }
    //
    //    // 抢购
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
    //    // 进入到订单详情页
    //    return "orderDetail";
    //}

    ///**
    // * 处理用户秒杀/抢购请求
    // *
    // * @param model   携带数据到下一个页面
    // * @param user    用户
    // * @param goodsId 秒杀商品id
    // * @return 秒杀结果页面
    // * V2.0改进版
    // */
    //@RequestMapping("/doSeckill")
    //public String doSeckill(Model model, User user, Long goodsId) {
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
    //    // PRO: 解决复购问题：判断用户是否是复购，直接到redis获取对应的秒杀订单
    //    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
    //    if (null != seckillOrder) { // 不为null，则说明该用户已经抢购该商品，则返回错误页面
    //        model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
    //        return "secKillFail";
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
    //    // 进入到订单详情页
    //    return "orderDetail";
    //}

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
    //    // TODO: 获取到goodsVo
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    // 判断库存
    //    if (goodsVo.getStockCount() < 1) {  // 没有库存
    //        model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK.getMessage());
    //        return "secKillFail";   // 错误页面
    //    }
    //
    //    // TODO: 判断用户是否是复购，判断当前购买用户id和购买商品id是否已经在商品秒杀表存在
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
}
