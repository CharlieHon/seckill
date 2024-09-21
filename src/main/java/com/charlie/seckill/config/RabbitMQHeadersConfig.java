package com.charlie.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.ExhaustedRetryException;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ：配置类，可以创建队列、交换机
 */
@Configuration
public class RabbitMQHeadersConfig {

    // 定义队列名，交换机，路由
    private static final String QUEUE01 = "queue_headers01";
    private static final String QUEUE02 = "queue_headers02";
    private static final String EXCHANGE = "headersExchange";

    // 创建/配置队列
    @Bean
    public Queue queue_headers01() {
        return new Queue(QUEUE01);
    }

    @Bean
    public Queue queue_headers02() {
        return new Queue(QUEUE02);
    }

    // 创建交换机
    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(EXCHANGE);
    }

    // 将队列绑定到交换机，同时声明要匹配的k-v，和以什么方式匹配(all/any)
    @Bean
    public Binding binding_header01() {
        // 先定义/声明k-v，可以右多个，所以将其放入map。这里的k-v由业务逻辑决定
        Map<String, Object> map = new HashMap<>();
        map.put("color", "red");
        map.put("speed", "low");
        // whereAny表示k-v有任何一个匹配即可
        return BindingBuilder.bind(queue_headers01()).to(headersExchange()).whereAny(map).match();
    }

    @Bean
    public Binding binding_header02() {
        // 先定义/声明k-v，可以右多个，所以将其放入map。这里的k-v由业务逻辑决定
        Map<String, Object> map = new HashMap<>();
        map.put("color", "red");
        map.put("speed", "fast");
        // whereAll表示k-v全部匹配
        return BindingBuilder.bind(queue_headers02()).to(headersExchange()).whereAll(map).match();
    }
}
