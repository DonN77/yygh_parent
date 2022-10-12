package com.donn.yygh.hosp.controller.api;

import com.donn.yygh.hosp.bean.Result;
import com.donn.yygh.hosp.service.ScheduleService;
import com.donn.yygh.hosp.utils.HttpRequestHelper;
import com.donn.yygh.model.hosp.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/27 21:20
 **/
@RestController
@RequestMapping("/api/hosp")
public class ApiScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.saveSchedule(map);
        return Result.ok();
    }

//    因为第三方医院请求方法返回值，需要封装data json对象，里面有 totalElements、content属性，所以Result的泛型是Page类型
    @PostMapping("/schedule/list")
    public Result<Page> getSchedulePage(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        Page<Schedule> page = scheduleService.getSchedulePage(map);
        return Result.ok(page);
    }

    @PostMapping("/schedule/remove")
    public Result removeSchedule(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.removeSchedule(map);
        return Result.ok();
    }
}
