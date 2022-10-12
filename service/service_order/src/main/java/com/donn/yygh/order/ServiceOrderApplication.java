package com.donn.yygh.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/9 16:00
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"com.donn"})
@MapperScan("com.donn.yygh.order.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.donn"})
public class ServiceOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class,args);
    }
}
