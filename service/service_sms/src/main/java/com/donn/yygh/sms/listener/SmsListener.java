package com.donn.yygh.sms.listener;

import com.donn.yygh.mq.MqConst;
import com.donn.yygh.sms.service.SmsService;
import com.donn.yygh.vo.msm.MsmVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/10 9:12
 **/
@Component
public class SmsListener {

    @Autowired
    private SmsService smsService;

    @RabbitListener(  //监听队列
            bindings = {
                    @QueueBinding(  //在消费者端创建队列、交换机，绑定交换机和队列
                            value = @Queue(name = MqConst.QUEUE_SMS_ITEM),   //创建队列
                            exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_SMS),  //创建交换机
                            key = MqConst.ROUTING_SMS_ITEM   //绑定队列和交换机
                    )
            }
    )
    //监听队列，有消息了则消费消息
    public void consume(MsmVo msmVo, Message message){
        smsService.sendMessage(msmVo);
    }
}
