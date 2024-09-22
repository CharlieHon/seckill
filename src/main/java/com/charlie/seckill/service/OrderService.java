package com.charlie.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.charlie.seckill.pojo.Order;
import com.charlie.seckill.pojo.User;
import com.charlie.seckill.vo.GoodsVo;

public interface OrderService extends IService<Order> {
    // 方法：秒杀
    Order seckill(User user, GoodsVo goodsVo);

    // 方法：生成秒杀路径/值(唯一)
    String createPath(User user, Long goodsId);

    // 方法：对秒杀路径进行叫校验
    boolean checkPath(User user, Long goodsId, String path);
}
