package com.donn.yygh.order.client;

import com.donn.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/12 9:53
 **/
@FeignClient(value = "service-orders")
public interface OrderInfoFeignClient {

    //与被调用方法，请求路径一致、请求参数一致、返回参数一致
    @PostMapping("/api/order/orderInfo/statistics")
    //因为使用openfeign进行远程调用，数据传输是使用json格式的，所以需要使用 @RequestBody来接收传输的json对象数据
    //使用@RequestBody 注解，不能用GetMapping
    public Map<String, Object> statistics(@RequestBody OrderCountQueryVo orderCountQueryVo);
}
