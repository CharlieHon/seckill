package com.charlie.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;


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

    /*
    ---topic---
     */
    @RabbitListener(queues = "queue_topic01")
    public void receive_topic1(Object msg) {
        log.info("从队列 queue_topic01 接收到消息-->" + msg);
    }

    @RabbitListener(queues = "queue_topic02")
    public void receive_topic2(Object msg) {
        log.info("从队列 queue_topic02 接收到消息-->" + msg);
    }

    /*
    ---headers---
     */
    @RabbitListener(queues = "queue_headers01")
    public void receive_header01(Message message) {
        log.info("queue_headers01:接收到消息对象-->" + message);
        log.info("queue_headers01:接收到消息内容-->" + new String(message.getBody()));
    }

    @RabbitListener(queues = "queue_headers02")
    public void receive_header02(Message message) {
        log.info("queue_headers02:接收到消息对象-->" + message);
        log.info("queue_headers02:接收到消息内容-->" + new String(message.getBody()));
    }
}
