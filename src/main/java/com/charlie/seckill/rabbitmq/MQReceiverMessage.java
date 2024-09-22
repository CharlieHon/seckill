package com.charlie.seckill.rabbitmq;

import cn.hutool.json.JSONUtil;
import com.charlie.seckill.pojo.Order;
import com.charlie.seckill.pojo.SeckillMessage;
import com.charlie.seckill.pojo.User;
import com.charlie.seckill.service.GoodsService;
import com.charlie.seckill.service.OrderService;
import com.charlie.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * MQReceiverMessage: 消息的接收者/消费者，在这里调用seckill()方法
 */
@Slf4j
@Service
public class MQReceiverMessage {

    // 装配需要的组件/对象
    @Resource
    private GoodsService goodsService;

    @Resource
    private OrderService orderService;

    // 接收消息，并完成下单
    @RabbitListener(queues = "seckillQueue")
    public void receive(String message) {
        log.info("接收到的消息--->" + message);
        // 从队列中取出的是String，但是需要的是SeckillMessage，因此需要一个工具类JSONUtil
        SeckillMessage seckillMessage = JSONUtil.toBean(message, SeckillMessage.class);
        // 秒杀用户对象
        User user = seckillMessage.getUser();
        // 秒杀商品id
        Long goodsId = seckillMessage.getGoodsId();
        // 通过商品id，获取对应的GoodsVo
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        // 下单操作
        orderService.seckill(user, goodsVo);
    }

}
