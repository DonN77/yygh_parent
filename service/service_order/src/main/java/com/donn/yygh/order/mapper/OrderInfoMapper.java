package com.donn.yygh.order.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.donn.yygh.model.order.OrderInfo;
import com.donn.yygh.vo.order.OrderCountQueryVo;
import com.donn.yygh.vo.order.OrderCountVo;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author donn
 * @since 2022-10-09
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    List<OrderCountVo> statistics(OrderCountQueryVo orderCountQueryVo);
}
