package com.charlie.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charlie.seckill.mapper.OrderMapper;
import com.charlie.seckill.pojo.Order;
import com.charlie.seckill.pojo.SeckillGoods;
import com.charlie.seckill.pojo.SeckillOrder;
import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.OrderService;
import com.charlie.seckill.service.SeckillGoodsService;
import com.charlie.seckill.service.SeckillOrderService;
import com.charlie.seckill.vo.GoodsVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    // 装配需要的组件/对象
    @Resource
    private SeckillGoodsService seckillGoodsService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private SeckillOrderService seckillOrderService;

    @Resource
    private RedisTemplate redisTemplate;

    // 完成秒杀
    @Override
    @Transactional
    public Order seckill(User user, GoodsVo goodsVo) {
        // 查询秒杀商品库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));

        // 完成一个基本的秒杀操作[这块操作不具有原子性]，后续在高并发的情况下还会优化
        // 1> 这里操作不具有原子性，比如说有200个请求到达这里，其中20个进行get操作时拿到的库存量是相同的，那个这20个请求只会进行一次修改
        //seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        //seckillGoodsService.updateById(seckillGoods);

        // 分析：解决超卖现象
        // 1. Mysql 在默认的事务隔离级别[REPEATABLE-READ]下
        // 2. 执行update语句时，回在事务中锁定要更新的行
        // 3. 这样就可以防止其它会话在同一行执行update，delete

        // 只有在更新成功时，update为true，否则返回false，即更新后受影响的行数>1为T
        boolean update = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count=stock_count-1 ")
                .eq("goods_id", goodsVo.getId())
                .gt("stock_count", 0));
        if (!update) {  // 如果更新失败，说明当前商品已经没有库存，则不生产订单
            return null;
        }

        // 生成普通订单，2> 以下代码总会执行200次，因此订单会是200个，但是只超卖10个
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);    // 设置一个初始值
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());    // 设置为秒杀价
        order.setOrderChannel(1);       // 设置一个初始值
        order.setStatus(0);             // 初始值，未支付
        order.setCreateDate(new Date());    // 设置为当前时间

        // 保存order信息
        orderMapper.insert(order);

        // 生成秒杀商品订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(seckillGoods.getGoodsId());
        // 秒杀商品订单对应的order_id是从上面添加order后获取的
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());

        // 保存seckillOrder信息
        seckillOrderService.save(seckillOrder);

        // 将生成的秒杀订单，存入到redis，这样在查询某个用户是否已经秒杀了该商品时，直接到redis中查询，这起到优化效果
        // 设计秒杀订单的key：order:用户id:商品id
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goodsVo.getId(), seckillOrder);

        return order;
    }
}
