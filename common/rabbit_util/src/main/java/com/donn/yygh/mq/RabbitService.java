package com.donn.yygh.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/9 23:42
 **/
@Component
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     *  往rabbitMQ里直接发送字符串，或者字符串.getBytes()是可以的
     *  要想往rabbitMQ里直接发送 pojo类对象.getBytes() 也是可以的
     *  但是如果想直接发送 pojo类对象的json数据，需要自定义一个配置类，配置类往容器中注册一个 消息转换器（MessageConverter）
     */
    public boolean sendMessage(String exchange, String routingKey, Object message){
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        return true;
    }
}
