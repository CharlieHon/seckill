package com.charlie.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * MQSender：消息的发送者/生产者
 */
@Service
@Slf4j
public class MQSender {

    // 装配RabbitTemplate->操作RabbitMQ
    @Resource
    private RabbitTemplate rabbitTemplate;

    // 方法：发送消息
    public void send(Object msg) {
      log.info("发送消息-->" + msg);
      // "queue"队列名
      rabbitTemplate.convertAndSend("queue", msg);
    }

    // 方法：发送信息到fanout交换机
    public void sendFanout(Object msg) {
        log.info("fanout:发送消息-->" + msg);
        rabbitTemplate.convertAndSend("fanoutExchange", "", msg);
    }

    // 发送消息到direct交换机，同时指定路由
    public void sendDirect(Object msg, String routingKey) {
        log.info("direct:发送信息-->" + msg);
        rabbitTemplate.convertAndSend("directExchange", routingKey, msg);
    }

    // 发送消息到topic交换机，同时指定路由 queue.red.
    public void sendTopic(Object msg, String routingKey) {
        log.info("topic:发送消息-->" + msg);
        rabbitTemplate.convertAndSend("topicExchange", routingKey, msg);
    }

    // 发送消息到headers交换机，同时指定 匹配的k-v
    public void sendHeader01(String msg) {
        log.info("headers:发送消息-->" + msg);
        // 创建消息属性MessageProperties
        MessageProperties properties = new MessageProperties();
        properties.setHeader("color", "red");
        properties.setHeader("speed", "fast");
        // 创建Message对象，包含了发送的消息本身和属性
        Message message = new Message(msg.getBytes(), properties);
        rabbitTemplate.convertAndSend("headersExchange", "", message);
    }

    public void sendHeader02(String msg) {
        log.info("headers:发送消息-->" + msg);
        // 创建消息属性MessageProperties
        MessageProperties properties = new MessageProperties();
        properties.setHeader("color", "red");
        properties.setHeader("speed", "normal");
        // 创建Message对象，包含了发送的消息本身和属性
        Message message = new Message(msg.getBytes(), properties);
        rabbitTemplate.convertAndSend("headersExchange", "", message);
    }
}
