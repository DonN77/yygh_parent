package com.donn.yygh.hosp.listener;

import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.hosp.service.ScheduleService;
import com.donn.yygh.mq.MqConst;
import com.donn.yygh.mq.RabbitService;
import com.donn.yygh.vo.msm.MsmVo;
import com.donn.yygh.vo.order.OrderMqVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/10 7:39
 **/
@Component
public class OrderMqListener {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = MqConst.QUEUE_ORDER,durable = "true"),  //创建队列
                    exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_ORDER),   //创建交换机
                    key = MqConst.ROUTING_ORDER  //绑定交换机和队列
            )
    })
    //消费者的监听队列方法，从MQ容器中获取 orderMqVo pojo对象
    //确认挂号：走的是这个方法，available传值了，是第三方医院提供的值 -n
    //取消挂号：走的也是这个方法，available没有传值，available + 1了
    //如果available没有值，就是表示 取消挂号
    public void consume(OrderMqVo orderMqVo){
        String scheduleId = orderMqVo.getScheduleId();
        Integer availableNumber = orderMqVo.getAvailableNumber();
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (availableNumber != null){
            boolean flag = scheduleService.updateAvailableNumber(scheduleId, availableNumber);
        }else {
            scheduleService.cancelSchedule(scheduleId);
        }

        if (msmVo != null){
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SMS, MqConst.ROUTING_SMS_ITEM, msmVo);
        }
    }
}
