package com.donn.yygh.order.config;

import com.donn.yygh.mq.MqConst;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/10 7:06
 **/

//@Configuration
/**
 *  在生产者一端，创建交换机、队列、交换机和队列绑定关系
 *  项目没有使用这种方式
 *  而是在消费者一端，使用注解方式进行创建
 *  所以我注释掉@Configuration 注解
 */
public class OrderConfig {

    @Bean
    public Exchange getExchange(){
        return ExchangeBuilder.directExchange(MqConst.EXCHANGE_DIRECT_ORDER).durable(true).build();
    }
    @Bean
    public Queue getQueue(){
        return QueueBuilder.durable(MqConst.QUEUE_ORDER).build();
    }
    @Bean
    public Binding binding(@Qualifier("getQueue") Queue queue,@Qualifier("getExchange") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(MqConst.ROUTING_ORDER).noargs();
    }
}
