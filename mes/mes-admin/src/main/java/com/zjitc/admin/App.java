package com.zjitc.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.zjitc")
@MapperScan("**.mapper")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        System.out.println("================== 启动成功 ==================");
    }
}