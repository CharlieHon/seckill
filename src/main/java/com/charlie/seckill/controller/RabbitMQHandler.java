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
}
