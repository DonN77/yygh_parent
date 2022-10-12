package com.donn.yygh.hosp.controller.user;

import com.donn.yygh.common.result.R;
import com.donn.yygh.hosp.service.ScheduleService;
import com.donn.yygh.model.hosp.Schedule;
import com.donn.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/8 15:44
 **/
@RestController
@RequestMapping("/user/hosp/schedule")
public class UserScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    //用户系统，排版信息按照日期展示
    @GetMapping("/{hoscode}/{depcode}/{pageNum}/{pageSize}")
    public R getSchedulePage(@PathVariable String hoscode,
                             @PathVariable String depcode,
                             @PathVariable Integer pageNum,
                             @PathVariable Integer pageSize){

        Map<String,Object> map = scheduleService.getSchedulePageByCondition(hoscode,depcode,pageNum,pageSize);
        return R.ok().data(map);
    }

    //通过hoscode、depcode、workdate获取 指定医院指定科室的workDate当天的所有排班
    @GetMapping("/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetail(@PathVariable String hoscode,
                               @PathVariable String depcode,
                               @PathVariable String workDate){

        List<Schedule> details = scheduleService.detail(hoscode, depcode, workDate);
        return R.ok().data("details",details);
    }

    //根据排班id获取排班信息
    @GetMapping("/info/{scheduleId}")
    public R detailByScheduleId(@PathVariable String scheduleId){
        Schedule schedule = scheduleService.detailByScheduleId(scheduleId);
        return R.ok().data("schedule",schedule);
    }

    //远程调用通过scheduleId获取 订单上的关于排班信息
    //PathVariable的value要带上
    @GetMapping("/{scheduleId}")
    public ScheduleOrderVo getScheduleById(@PathVariable("scheduleId") String scheduleId){
        return scheduleService.getScheduleById(scheduleId);
    }
}
