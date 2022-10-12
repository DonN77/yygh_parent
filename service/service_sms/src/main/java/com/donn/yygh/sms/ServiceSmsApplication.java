package com.donn.yygh.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/4 21:57
 **/
@ComponentScan("com.donn")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceSmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSmsApplication.class,args);
    }
}
