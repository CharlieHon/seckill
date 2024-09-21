package com.charlie.seckill.controller;

import com.charlie.seckill.rabbitmq.MQSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class RabbitMQHandler {

    // 装配MQSender
    @Resource
    private MQSender mqSender;

    // 方法：调用消息生产者，发送消息
    @ResponseBody
    @RequestMapping("/mq")
    public void mq() {
        mqSender.send("Hello, world!");
    }

    // 方法：调用消息生产者，发送消息到交换机(fanoutExchange)
    @ResponseBody
    @RequestMapping("/mq/fanout")
    public void fanout() {
        mqSender.sendFanout("hello, fanout~");
    }

    // 方法：调用消息生产者，发送消息到交换机(directExchange)
    @ResponseBody
    @RequestMapping("/mq/direct/{routingKey}")
    public void direct(@PathVariable(value = "routingKey") String routingKey) {
        mqSender.sendDirect("direct:hello, charlie", routingKey);
    }

    // 方法：调用消息生产者，发送消息到交换机(topicExchange)
    @ResponseBody
    @RequestMapping("/mq/topic/{routingKey}")
    public void topic(@PathVariable(value = "routingKey") String routingKey) {
        mqSender.sendTopic("topic:hello, world!", routingKey);
    }

    // 方法：调用消息生产者，发送消息到交换机(headersExchange)
    @ResponseBody
    @RequestMapping("/mq/headers01")
    public void headers01() {
        // 这条消息使两个队列都接收到
        mqSender.sendHeader01("hello ABC");
    }

    @ResponseBody
    @RequestMapping("/mq/headers02")
    public void headers02() {
        // 这条消息仅queue_headers01接收到
        mqSender.sendHeader02("hello charlie");
    }
}
