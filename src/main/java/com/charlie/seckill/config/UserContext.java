package com.charlie.seckill.config;

import com.charlie.seckill.pojo.User;

public class UserContext {

    // 每个线程都有自己的ThreadLocal，把共享数据存放在这里，保证线成安全
    private static ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static User getUser() {
        return userHolder.get();
    }

}
