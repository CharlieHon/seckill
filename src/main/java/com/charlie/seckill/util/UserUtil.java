package com.charlie.seckill.util;

import com.charlie.seckill.pojo.User;
import com.charlie.seckill.vo.RespBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成多用户测试脚本：
 * 1. 创建多个用户，保存到 seckill_user 表
 * 2. 模拟http请求，生成jmeter压测脚本
 */
public class UserUtil {

    public static void create(int count) throws Exception {
        List<User> users = new ArrayList<>();
        // 创建 count 个用户
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId(13300000100L + i);
            user.setNickname("user" + i);
            user.setSalt("ptqtXy16");  // 用户数据表的salt，由程序员设置
            // 12346是用户原始密码
            user.setPassword(MD5Util.inputPassToDBPass("12346", user.getSalt()));
            users.add(user);
        }
        System.out.println("create user");

        // 将数据插入到数据库 seckill_user
        Connection connection = getConn();
        String sql = "insert into seckill_user(nickname, salt, password, id) values (?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            preparedStatement.setString(1, user.getNickname()); // 昵称
            preparedStatement.setString(2, user.getSalt());     // 盐
            preparedStatement.setString(3, user.getPassword()); // 密码
            preparedStatement.setLong(4, user.getId());         // 电话号码
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        preparedStatement.clearParameters();
        connection.close();
        System.out.println("insert to do");

        // 登录拿到userTicket
        String urlStr = "http://localhost:8080/login/doLogin";
        File file = new File("F:\\temp\\config.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            // 请求
            URL url = new URL(urlStr);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            // 设置输入网页密码（相当于输出到页面）
            co.setDoOutput(true);
            OutputStream outputStream = co.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToMidPass("12346");
            outputStream.write(params.getBytes());
            outputStream.flush();

            // 获取网页输出，得到输入流，把结果得到，再输出到ByteArrayOutputStream内
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) >= 0) {
                bout.write(bytes, 0, len);
            }
            inputStream.close();
            bout.close();

            // 把ByteArrayOutputStream内的东西转换为respBean对象
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();   // 将json数据转化为对象
            RespBean respBean = mapper.readValue(response, RespBean.class);
            // 得到userTicket
            String userTicket = (String) respBean.getObj();
            System.out.println("create userTicket" + userTicket);
            String row = user.getId() + "," + userTicket;
            // 写入指定文件
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file:" + user.getId());
        }
        raf.close();
        System.out.println("over");
    }

    private static Connection getConn() throws SQLException, ClassNotFoundException {
        String url = "jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "hsp";
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    public static void main(String[] args) throws Exception {
        create(1);   // 创建了2000个测试用户
    }

}
