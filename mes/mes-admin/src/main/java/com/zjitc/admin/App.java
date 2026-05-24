package com.zjitc.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.zjitc"})  // 确保扫描到framework包
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}