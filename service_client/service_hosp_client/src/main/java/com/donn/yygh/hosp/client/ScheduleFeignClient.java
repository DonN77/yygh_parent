package com.donn.yygh.hosp.client;

import com.donn.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/9 11:19
 **/
@FeignClient(value = "service-hosp")
public interface ScheduleFeignClient {

    //远程调用通过scheduleId获取 订单上的关于排班信息
    //PathVariable的value要带上
    @GetMapping("/user/hosp/schedule/{scheduleId}")
    public ScheduleOrderVo getScheduleById(@PathVariable("scheduleId") String scheduleId);

}
