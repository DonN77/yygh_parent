package com.donn.yygh.order.service;

import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/10 15:55
 **/
public interface WeiPayService {
    String createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId);

    void paySuccess(Long orderId, Map<String, String> map);

    boolean refund(Long orderId);
}
