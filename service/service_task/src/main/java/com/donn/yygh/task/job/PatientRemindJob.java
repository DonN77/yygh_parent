package com.donn.yygh.task.job;

import com.donn.yygh.mq.MqConst;
import com.donn.yygh.mq.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/11 18:31
 **/
@Component
public class PatientRemindJob {
    @Autowired
    private RabbitService rabbitService;

    @Scheduled(cron = "0 0 6 * * *")
    public void remindTime(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8," ");
    }
}
