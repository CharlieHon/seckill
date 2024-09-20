package com.charlie.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ：配置类，可以创建队列、交换机
 */
@Configuration
public class RabbitMQConfig {

    // 定义队列名
    private static final String QUEUE = "queue";

    /**
     * 1. 创建队列
     * 2. 队列名QUEUE(queue)
     * 3. true: 表示持久化
     * durable：表示队列是否持久化
     * 队列在默认情况下放到内存，rabbitmq重启就丢失了，如果希望重启后，队列还能使用，就需要持久话
     * Erlang自带Mnesia数据库，当rabbitmq重启后，会读取该数据库
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }

}
