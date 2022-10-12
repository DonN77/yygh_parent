package com.donn.yygh.order.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.donn.yygh.common.result.R;
import com.donn.yygh.enums.OrderStatusEnum;
import com.donn.yygh.enums.PaymentStatusEnum;
import com.donn.yygh.model.order.OrderInfo;
import com.donn.yygh.model.order.PaymentInfo;
import com.donn.yygh.order.prop.WeiPayProperties;
import com.donn.yygh.order.service.OrderInfoService;
import com.donn.yygh.order.service.PaymentService;
import com.donn.yygh.order.service.WeiPayService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/10 14:50
 **/
@RestController
@RequestMapping("/user/order/weixin")
public class WeiPayController {
    @Autowired
    private WeiPayService weiPayService;

    //返回创建二维码的url
    @GetMapping("/{orderId}")
    public R createNative(@PathVariable Long orderId) {
        String url = weiPayService.createNative(orderId);
        return R.ok().data("url", url);
    }

    //查询订单支付状态
    //前端每隔3秒，请求这个方法
    //如果是支付中，过了3秒继续请求
    //支付成功则无需再访问
    @GetMapping("/status/{orderId}")
    public R getPayStatus(@PathVariable Long orderId) {
        //因为微信后台返回的就是一个map，所以直接返回一个map就行
        Map<String, String> map = weiPayService.queryPayStatus(orderId);
        if (map == null) {
            return R.error().message("查询失败");
        }
        if ("SUCCESS".equals(map.get("trade_state"))){  //支付成功
            //更新订单表的订单状态
            //更新支付记录表的支付状态
            //这两步操作要保证原子性，所以需要提取到service层，给这个方法加事务
            weiPayService.paySuccess(orderId,map);
            return R.ok();
        }
        return R.ok().message("支付中");  //也包括了支付失败的情况

    }
}
