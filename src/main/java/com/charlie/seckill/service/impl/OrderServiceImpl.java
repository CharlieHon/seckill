package com.charlie.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.stereotype.Service;

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

    // 完成秒杀
    @Override
    public Order seckill(User user, GoodsVo goodsVo) {
        // 查询秒杀商品库存，并-1
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));

        // 完成一个基本的秒杀操作[这块操作不具有原子性]，后续在高并发的情况下还会优化
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        seckillGoodsService.updateById(seckillGoods);

        // 生成普通订单
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

        return order;
    }
}
