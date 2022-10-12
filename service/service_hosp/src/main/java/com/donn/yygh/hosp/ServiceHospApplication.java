package com.donn.yygh.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/21 20:03
 **/
@SpringBootApplication
@MapperScan({"com.donn.yygh.hosp.mapper"})
@ComponentScan({"com.donn.yygh"})   //需要指定扫描包路径，因为默认只是扫描当前模块，配置之后会把依赖的模块的该路径也扫描
@EnableDiscoveryClient   //表示支持使用nacos，在nacos上注册
@EnableFeignClients(basePackages = "com.donn.yygh")   //开启openfeign功能，而且要设置扫描包路径
public class ServiceHospApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class,args);
    }
}
