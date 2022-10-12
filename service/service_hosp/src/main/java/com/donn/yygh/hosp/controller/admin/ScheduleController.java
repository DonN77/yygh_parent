package com.donn.yygh.hosp.controller.admin;

import com.donn.yygh.common.result.R;
import com.donn.yygh.hosp.service.ScheduleService;
import com.donn.yygh.model.hosp.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/1 21:06
 **/
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/{pageNum}/{pageSize}/{hoscode}/{depcode}")
    public R page(@PathVariable Integer pageNum, @PathVariable Integer pageSize,
                                @PathVariable String hoscode, @PathVariable String depcode){
        Map<String,Object> map = scheduleService.page(pageNum,pageSize,hoscode,depcode);
        return R.ok().data(map);
    }

    @GetMapping("/{hoscode}/{depcode}/{workDate}")
    public R detail(@PathVariable String hoscode,@PathVariable String depcode,@PathVariable String workDate){
        List<Schedule> list = scheduleService.detail(hoscode,depcode,workDate);
        return R.ok().data("list",list);
    }
}
