package com.charlie.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.charlie.seckill.pojo.Order;
import com.charlie.seckill.pojo.User;
import com.charlie.seckill.vo.GoodsVo;

public interface OrderService extends IService<Order> {
    // 方法：秒杀
    Order seckill(User user, GoodsVo goodsVo);
}
