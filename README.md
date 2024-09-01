# 秒杀/高并发解决方案

技术栈：`SpringBoot` `Mysql` `Redis` `RabbitMQ` `Mybatis-Plus` `Maven` `Linux` `Jmeter`

## 1. 项目介绍

1. 秒杀/高并发主要解决两个问题：**并发读**、**并发写**
2. 并发读的核心优化理念是**尽量减少用户到DB来"读"数据**，或者让他们**读更少的数据**, 并发写的处理原则也一样
3. 针对秒杀系统需要做一些保护，针对意料之外的情况设计**兜底方案**，以防止最坏的情况发生
4. 系统架构要满足**高可用**: 流量符合预期时要稳定，要保证秒杀活动顺利完成，即秒杀商品顺利地卖出去，这个是最基本的前提
5. 系统保证**数据的一致性**: 就是秒杀 10 个 商品 ，那就只能成交 10 个商品，多一个少一都不行。一旦库存不对，就要承担损失
6. 系统要满足**高性能**: 也就是系统的性能要足够高，需要支撑大流量, 不光是服务端要做极致的性能优化，而且在整个请求链路上都要做协同的优化，每个地方快一点, 整个系统就快了
7. 、秒杀涉及大量的并发读和并发写，因此**支持高并发访问**这点非常关键，对应的方案比如页面缓存、`Redis`预减库存/内存标记与隔离、请求的削峰（`RabbitMQ`/异步请求）、分布式`Session`共享等。

## 2. 秒杀系统-项目搭建

略

## 3. 分布式会话/Session

### 用户登录

用户密码设计：两次加密加盐`md5(md5(pass明文+固定salt)+salt2)`

### 分布式session共享

## 4. 秒杀基本功能开发

## 5. 秒杀压力测试

## 6. 页面优化

## 7. 秒杀-复购和超卖

## 8. 秒杀优化-RabbitMQ+Redis

## 9. 秒杀安全

## 10. Redis分布式锁探讨