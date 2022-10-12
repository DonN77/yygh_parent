package com.donn.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donn.yygh.enums.PaymentStatusEnum;
import com.donn.yygh.model.order.OrderInfo;
import com.donn.yygh.model.order.PaymentInfo;
import com.donn.yygh.order.mapper.PaymentMapper;
import com.donn.yygh.order.service.PaymentService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/10 14:48
 **/
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService{
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void savePaymentInfo(OrderInfo order, Integer paymentType) {

        String url = (String) redisTemplate.opsForValue().get(order.getId().toString());
        if (!StringUtils.isEmpty(url)){
            return;
        }
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",order.getId());
        queryWrapper.eq("payment_status",PaymentStatusEnum.UNPAID.getStatus());
        queryWrapper.eq("payment_type",paymentType);
        //先查询 订单支付表中，该订单是否有未支付记录，防止用户点击多次，导致表中生成多条未支付记录
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);
        //如果不为空，表示在支付表中已经有该订单未支付记录，则直接返回
        if (paymentInfo != null){
            return;
        }

        paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setTotalAmount(order.getAmount());

        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"+order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
        paymentInfo.setSubject(subject);

        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());

        baseMapper.insert(paymentInfo);
    }
}
