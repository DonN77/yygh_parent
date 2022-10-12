package com.donn.yygh.mq;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/9 23:48
 **/
@Configuration
public class RabbitConfig {

    //作用：生产者将发送到RabbitMQ中的pojo对象，自动进行转换，转换成json格式存储
    //     消费者从rabbitMQ中消费消息时，会自动将 json格式数据转换为pojo对象类型
    @Bean
    public MessageConverter getMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
