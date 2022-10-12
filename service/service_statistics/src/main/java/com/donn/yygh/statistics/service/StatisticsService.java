package com.donn.yygh.statistics.service;

import com.donn.yygh.vo.order.OrderCountQueryVo;

import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/12 9:20
 **/
public interface StatisticsService {
    Map<String, Object> statistics(OrderCountQueryVo orderCountQueryVo);
}
