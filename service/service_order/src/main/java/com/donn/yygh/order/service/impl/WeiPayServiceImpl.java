package com.donn.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.enums.OrderStatusEnum;
import com.donn.yygh.enums.PaymentStatusEnum;
import com.donn.yygh.enums.PaymentTypeEnum;
import com.donn.yygh.enums.RefundStatusEnum;
import com.donn.yygh.model.order.OrderInfo;
import com.donn.yygh.model.order.PaymentInfo;
import com.donn.yygh.model.order.RefundInfo;
import com.donn.yygh.order.prop.WeiPayProperties;
import com.donn.yygh.order.service.OrderInfoService;
import com.donn.yygh.order.service.PaymentService;
import com.donn.yygh.order.service.RefundInfoService;
import com.donn.yygh.order.service.WeiPayService;
import com.donn.yygh.order.utils.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/10 15:54
 **/
@Service
public class WeiPayServiceImpl implements WeiPayService {
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private WeiPayProperties weiPayProperties;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RefundInfoService refundInfoService;

    @Override
    //https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_1 ，相关操作参考微信官方文档
    //返回url地址，前端用于生成微信支付二维码
    public String createNative(Long orderId) {

        //去redis里面查
        String payUrl =(String) redisTemplate.opsForValue().get(orderId.toString());
        if (!StringUtils.isEmpty(payUrl)) return payUrl;

        //1.根据orderId去数据库中获取订单信息
        OrderInfo orderInfo = orderInfoService.getById(orderId);
        //2.保存支付记录信息
        paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
        //3.请求微信服务器获取微信支付的url地址(使用HttpClient远程调用)
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        //封装请求参数
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("appid",weiPayProperties.getAppid());
        paramMap.put("mch_id", weiPayProperties.getPartner());
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
        //sign，虽然微信平台需要传，用申请微信支付时，直接设置成了null，所以可以不用传

        Date reserveDate = orderInfo.getReserveDate();
        String reserveDateString = new DateTime(reserveDate).toString("yyyy/MM/dd");
        String body = reserveDateString + "就诊"+ orderInfo.getDepname();
        paramMap.put("body", body);
        paramMap.put("out_trade_no",orderInfo.getOutTradeNo());
        //微信后台 total_fee 表示收金额多少，单位是分
        //因为我要测试功能，所以设置成了 1分，如果是真实价格，我会心疼的
        //按理说 是 order.getAmount().multip(100);  乘上100，分 化为 元
        paramMap.put("total_fee","1");
        paramMap.put("spbill_create_ip","127.0.0.1");
        //用户确认付款之后，由微信后台回调，去notify_url该地址，要求一定是公网地址，所以可以给一个假地址
        //由于回调地址，我给了一个假地址，用户不能通过回调方法，得知是否支付成功
        //就需要第二种方法：预约挂号平台主动请求微信后台，请求是否支付成功
        //需要前端，定义一个定时器，比如每隔3秒请求后端，让后端去请求微信平台，返回是否支付成功，查到结果就取消定时器
        paramMap.put("notify_url","http://guli.shop/api/order/weixinPay/weixinNotify");
        paramMap.put("trade_type","NATIVE");

        try {
            //微信平台要求使用xml格式，调用微信提供的工具类 将paramMap转换为xml格式数据
            String xml = WXPayUtil.generateSignedXml(paramMap, weiPayProperties.getPartnerkey());
            httpClient.setXmlParam(xml);
            //地址是https，需要支持https协议
            httpClient.setHttps(true);  //需要设置支持https协议

            httpClient.post();  //发送请求
            String content = httpClient.getContent();//获取结果
            //微信服务器会返回一个xml格式结果
            String url = WXPayUtil.xmlToMap(content).get("code_url");

            //设置过期时间，微信支付二维码2小时过期，可采取2小时未支付取消订单
            if (!StringUtils.isEmpty(url)){
                redisTemplate.opsForValue().set(orderId.toString(), url,20, TimeUnit.MINUTES);
            }

            //4.将url返回给前端
            return url;
        } catch (Exception e) {
            //出现异常，返回一个空字符串，表示获取不到 生成支付二维码的url
            return "";
        }
    }

    @Override
    //向微信后台 查询订单支付状态
    //参考 https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_2 ，微信查询订单文档
    public Map<String, String> queryPayStatus(Long orderId) {
        OrderInfo order = orderInfoService.getById(orderId);

        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("appid", weiPayProperties.getAppid());
        paramMap.put("mch_id", weiPayProperties.getPartner());
        paramMap.put("appid", weiPayProperties.getAppid());
        paramMap.put("out_trade_no", order.getOutTradeNo());
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());  //微信工具类提供的 生成随机字符串 方法
        //sign，申请微信服务的时候设置为空串，所以不用传

        try {
            String xml = WXPayUtil.generateSignedXml(paramMap, weiPayProperties.getPartnerkey());
            httpClient.setXmlParam(xml);
            httpClient.setHttps(true);  //支持https
            httpClient.post();  //发送请求

            //支付状态封装在 请求微信服务，返回的Map里
            //返回的Map是以xml格式的，所以需要调用微信工具类方法将xml格式字符串转Map
            return WXPayUtil.xmlToMap(httpClient.getContent());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void paySuccess(Long orderId, Map<String, String> map) {
        //更新订单表的订单状态
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());  //更新订单支付状态
        orderInfoService.updateById(orderInfo);
        //更新支付记录表的支付状态
        UpdateWrapper<PaymentInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id",orderId);
        updateWrapper.set("trade_no",map.get("transaction_id"));  //微信支付的订单号，微信服务器那边生成的
        updateWrapper.set("payment_status", PaymentStatusEnum.PAID.getStatus());  //更新支付记录的支付状态

        updateWrapper.set("callback_time",new Date());
        updateWrapper.set("callback_content", JSONObject.toJSONString(map));
        paymentService.update(updateWrapper);
    }

    @Override
    public boolean refund(Long orderId) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        PaymentInfo one = paymentService.getOne(queryWrapper);

        RefundInfo refundInfo = refundInfoService.saveRefundInfo(one);
        //已经退过款，防止用户多次快速点击的情况
        if (refundInfo.getRefundStatus() == RefundStatusEnum.REFUND.getStatus().intValue()){
            return true;
        }
        //执行微信退款
        //使用微信退款需要 证书
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");

        Map<String,String> paramMap = new HashMap<>(8);
        paramMap.put("appid",weiPayProperties.getAppid());       //公众账号ID
        paramMap.put("mch_id",weiPayProperties.getPartner());   //商户编号
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
        paramMap.put("transaction_id",one.getTradeNo()); //微信订单号
        paramMap.put("out_trade_no",one.getOutTradeNo()); //商户订单编号
        paramMap.put("out_refund_no","tk"+one.getOutTradeNo()); //商户退款单号
        //为了测试，之前给1分，现在退款也是1分
        paramMap.put("total_fee","1");
        paramMap.put("refund_fee","1");

        try {
            String xml = WXPayUtil.generateSignedXml(paramMap,weiPayProperties.getPartnerkey());
            httpClient.setXmlParam(xml);
            httpClient.setHttps(true);
            httpClient.setCert(true);  //因为微信退款需要使用证书，所以设置支持证书
            httpClient.setCertPassword(weiPayProperties.getPartner());  //设置证书密码，就是商户号
            httpClient.post();

            String content = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            if ("SUCCESS".equals(resultMap.get("result_code"))){   //微信退款成功
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackTime(new Date());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);

                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
