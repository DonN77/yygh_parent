package com.donn.yygh.statistics.service.impl;

import com.donn.yygh.order.client.OrderInfoFeignClient;
import com.donn.yygh.statistics.service.StatisticsService;
import com.donn.yygh.vo.order.OrderCountQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/12 9:21
 **/
@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    @Override
    public Map<String, Object> statistics(OrderCountQueryVo orderCountQueryVo) {
        return orderInfoFeignClient.statistics(orderCountQueryVo);
    }
}
