package com.donn.yygh.statistics.controller;

import com.donn.yygh.common.result.R;
import com.donn.yygh.statistics.service.StatisticsService;
import com.donn.yygh.vo.order.OrderCountQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/12 9:08
 **/
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {
    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/countByDate")
    public R statistics(OrderCountQueryVo orderCountQueryVo){
        Map<String,Object> map = statisticsService.statistics(orderCountQueryVo);
        return R.ok().data(map);
    }
}
