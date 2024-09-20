package com.charlie.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ：配置类，可以创建队列、交换机
 */
@Configuration
public class RabbitMQConfig {

    // 定义队列名
    private static final String QUEUE = "queue";

    // --fanout--
    private static final String QUEUE1 = "queue_fanout01";
    private static final String QUEUE2 = "queue_fanout02";
    private static final String EXCHANGE = "fanoutExchange";

    // --direct--
    private static final String QUEUE_DIRECT1 = "queue_direct01";
    private static final String QUEUE_DIRECT2 = "queue_direct02";
    private static final String EXCHANGE_DIRECT = "directExchange";

    // 路由
    private static final String ROUTING_KEY01 = "queue.red";
    private static final String ROUTING_KEY02 = "queue.blue";

    // ---direct---
    /**
     * 创建/配置队列 QUEUE_DIRECT1(queue_direct01)
     */
    @Bean
    public Queue queue_direct1() {
        return new Queue(QUEUE_DIRECT1);
    }

    /**
     * 创建/配置队列 QUEUE_DIRECT2(queue_direct02)
     */
    @Bean
    public Queue queue_direct2() {
        return new Queue(QUEUE_DIRECT2);
    }

    /**
     * 创建/配置交换机 EXCHANGE_DIRECT(directExchange)
     */
    @Bean
    public DirectExchange exchange_direct() {
        return new DirectExchange(EXCHANGE_DIRECT);
    }

    /**
     * 1. 将队列 queue_direct1()/queue_direct1 绑定到指定的交换机exchange_direct
     * 2. 同时声明/关联路由 ROUTING_KEY01(queue.red)
     * (1) 队列：queue_direct1()
     * (2) 交换机：exchange_direct()
     * (3) 路由 ROUTING_KEY01
     */
    @Bean
    public Binding binding_direct1() {
        return BindingBuilder.bind(queue_direct1()).to(exchange_direct()).with(ROUTING_KEY01);
    }

    /**
     * 1. 将队列 queue_direct2()/queue_direct2 绑定到指定的交换机exchange_direct
     * 2. 同时声明/关联路由 ROUTING_KEY02(queue.red)
     * (1) 队列：queue_direct2()
     * (2) 交换机：exchange_direct()
     * (3) 路由 ROUTING_KEY02
     */
    @Bean
    public Binding binding_direct2() {
        return BindingBuilder.bind(queue_direct2()).to(exchange_direct()).with(ROUTING_KEY02);
    }

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

    // ---fanout---
    // 创建队列 QUEUE1(queue_fanout01)
    @Bean
    public Queue queue1() {
        return new Queue(QUEUE1);
    }

    // 创建队列 QUEUE2(queue_fanout02)
    @Bean
    public Queue queue2() {
        return new Queue(QUEUE2);
    }

    // 创建/配置交换机 EXCHANGE(fanoutExchange)
    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange(EXCHANGE);
    }

    /**
     * 将队列 QUEUE1 绑定到 交换机
     */
    @Bean
    public Binding binding01() {
        return BindingBuilder.bind(queue1()).to(exchange());
    }

    /**
     * 将队列 QUEUE2 绑定到 交换机
     */
    @Bean
    public Binding binding02() {
        return BindingBuilder.bind(queue2()).to(exchange());
    }

}
