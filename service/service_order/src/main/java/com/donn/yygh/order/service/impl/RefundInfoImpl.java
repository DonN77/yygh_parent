package com.donn.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donn.yygh.enums.PaymentTypeEnum;
import com.donn.yygh.enums.RefundStatusEnum;
import com.donn.yygh.model.order.PaymentInfo;
import com.donn.yygh.model.order.RefundInfo;
import com.donn.yygh.order.mapper.RefundInfoMapper;
import com.donn.yygh.order.service.RefundInfoService;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/11 15:16
 **/
@Service
public class RefundInfoImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //根据orderId 到退款表中查询，该订单是否已经有记录
        //有记录则无需再新添一条记录，直接返回即可
        //防止用户同时打开多个退款页面，然后依次点击，在退款表中生成多条同一orderId的记录
        Long orderId = paymentInfo.getOrderId();
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        RefundInfo one = baseMapper.selectOne(queryWrapper);
        if (one != null)return one;

        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setOrderId(orderId);
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        refundInfo.setSubject("想退款");
        refundInfo.setPaymentType(PaymentTypeEnum.WEIXIN.getStatus());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());

        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
