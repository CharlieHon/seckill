package com.charlie.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


/**
 * MQReceiver：消息的接收者/消费者
 */
@Service
@Slf4j
public class MQReceiver {

    // 方法：接收消息
    @RabbitListener(queues = "queue")
    public void receive(Object msg) {
        log.info("接收到消息-->" + msg);
    }

    /*
    ---fanout---
     */
    // 监听队列 queue_fanout01
    @RabbitListener(queues = "queue_fanout01")
    public void receive1(Object msg) {
        log.info("从队列 queue_fanout01 接收到消息-->" + msg);
    }

    // 监听队列 queue_fanout02
    @RabbitListener(queues = "queue_fanout02")
    public void receive2(Object msg) {
        log.info("从队列 queue_fanout02 接收到消息-->" + msg);
    }

    /*
    ---direct---
     */
    // 监听队列 queue_direct01
    @RabbitListener(queues = "queue_direct01")
    public void receive_direct1(Object msg) {
        log.info("从队列 queue_direct01 接收到消息-->" + msg);
    }

    // 监听队列 queue_direct02
    @RabbitListener(queues = "queue_direct02")
    public void receive_direct2(Object msg) {
        log.info("从队列 queue_direct02 接收到消息-->" + msg);
    }
}
