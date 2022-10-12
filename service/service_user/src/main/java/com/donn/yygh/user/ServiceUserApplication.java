package com.donn.yygh.user;

import com.donn.yygh.user.prop.WeixinProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/3 17:33
 **/
@SpringBootApplication
@ComponentScan(basePackages = "com.donn")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.donn")
@MapperScan("com.donn.yygh.user.mapper")
@EnableConfigurationProperties(value = WeixinProperties.class)
public class ServiceUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApplication.class,args);
    }
}
