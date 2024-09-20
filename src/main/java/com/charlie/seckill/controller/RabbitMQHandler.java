package com.charlie.seckill.controller;

import com.charlie.seckill.rabbitmq.MQSender;
import org.springframework.stereotype.Controller;
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

}
