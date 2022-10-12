package com.donn.yygh.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.donn.yygh.model.order.OrderInfo;
import com.donn.yygh.vo.order.OrderCountQueryVo;
import com.donn.yygh.vo.order.OrderQueryVo;

import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author donn
 * @since 2022-10-09
 */
public interface OrderInfoService extends IService<OrderInfo> {

    Long submitOrder(String scheduleId, Long patientId);

    Page<OrderInfo> getOrderPage(Integer pageNum, Integer pageSize, OrderQueryVo queryVo);

    OrderInfo detail(Long orderId);

    void cancelOrder(Long orderId);

    void patientRemind();

    Map<String, Object> statistics(OrderCountQueryVo orderCountQueryVo);
}
