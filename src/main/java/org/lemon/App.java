package org.lemon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/01 17:06:39
 */
@Async
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan(basePackages = "org.lemon.mapper")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}