package cn.learn;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @program: quick-pay-shop
 * @description: SpringBoot启动入口
 * @author: chouchouGG
 * @create: 2024-10-15 09:50
 **/
@SpringBootApplication
@Configurable
@EnableScheduling // 用于开启 Spring 定时任务的功能
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
