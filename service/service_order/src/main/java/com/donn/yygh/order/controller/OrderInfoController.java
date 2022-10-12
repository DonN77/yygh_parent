package com.donn.yygh.order.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donn.yygh.common.result.R;
import com.donn.yygh.common.utils.JwtHelper;
import com.donn.yygh.enums.OrderStatusEnum;
import com.donn.yygh.model.order.OrderInfo;
import com.donn.yygh.order.service.OrderInfoService;
import com.donn.yygh.vo.order.OrderCountQueryVo;
import com.donn.yygh.vo.order.OrderQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author donn
 * @since 2022-10-09
 */
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;


    @PostMapping("/statistics")
    //因为使用openfeign进行远程调用，数据传输是使用json格式的，所以需要使用 @RequestBody来接收传输的json对象数据
    //使用@RequestBody 注解，不能用GetMapping
    public Map<String, Object> statistics(@RequestBody OrderCountQueryVo orderCountQueryVo){
        return orderInfoService.statistics(orderCountQueryVo);
    }

    @PostMapping("/{scheduleId}/{patientId}")
    public R submitOrder(@PathVariable String scheduleId,
                         @PathVariable Long patientId){

        Long orderId = orderInfoService.submitOrder(scheduleId,patientId);
        return R.ok().data("orderId",orderId);
    }

    @GetMapping("/{pageNum}/{pageSize}")
    public R getOrderPage(@PathVariable Integer pageNum,
                          @PathVariable Integer pageSize,
                          OrderQueryVo queryVo,
                          @RequestHeader String token){
        Long userId = JwtHelper.getUserId(token);
        queryVo.setUserId(userId);
        Page<OrderInfo> page = orderInfoService.getOrderPage(pageNum,pageSize,queryVo);
        return R.ok().data("page",page);
    }

    @GetMapping("/list")
    public R getStatusList(){
        List<Map<String, Object>> statusList = OrderStatusEnum.getStatusList();
        return R.ok().data("list",statusList);
    }

    @GetMapping("/{orderId}")
    public R detail(@PathVariable Long orderId){
        OrderInfo orderInfo = orderInfoService.detail(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }

    @GetMapping("/cancel/{orderId}")
    public R cancelOrder(@PathVariable Long orderId){
        orderInfoService.cancelOrder(orderId);
        return R.ok();
    }
}

