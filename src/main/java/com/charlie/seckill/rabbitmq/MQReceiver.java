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

}
