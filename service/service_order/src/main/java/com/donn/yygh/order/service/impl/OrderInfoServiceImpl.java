package com.donn.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.enums.OrderStatusEnum;
import com.donn.yygh.enums.PaymentStatusEnum;
import com.donn.yygh.hosp.client.ScheduleFeignClient;
import com.donn.yygh.model.order.OrderInfo;
import com.donn.yygh.model.order.PaymentInfo;
import com.donn.yygh.model.user.Patient;
import com.donn.yygh.mq.MqConst;
import com.donn.yygh.mq.RabbitService;
import com.donn.yygh.order.mapper.OrderInfoMapper;
import com.donn.yygh.order.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donn.yygh.order.service.PaymentService;
import com.donn.yygh.order.service.WeiPayService;
import com.donn.yygh.order.utils.HttpRequestHelper;
import com.donn.yygh.user.client.PatientFeignClient;
import com.donn.yygh.vo.hosp.ScheduleOrderVo;
import com.donn.yygh.vo.msm.MsmVo;
import com.donn.yygh.vo.order.OrderCountQueryVo;
import com.donn.yygh.vo.order.OrderCountVo;
import com.donn.yygh.vo.order.OrderMqVo;
import com.donn.yygh.vo.order.OrderQueryVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author donn
 * @since 2022-10-09
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private PatientFeignClient patientFeignClient;
    @Autowired
    private ScheduleFeignClient scheduleFeignClient;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private WeiPayService weiPayService;
    @Autowired
    private PaymentService paymentService;

    @Override
    //生成订单
    public Long submitOrder(String scheduleId, Long patientId) {
        //1.先根据schedule获取排班信息
        ScheduleOrderVo schedule = scheduleFeignClient.getScheduleById(scheduleId);
        //判断确认预约的时间 是否已经超过了 当天挂号截止时间
        if (new DateTime(schedule.getStopTime()).isBeforeNow()){
            throw new YyghException(20001,"超过了挂号截止时间");
        }
        //2.根据patientId获取就诊人信息
        Patient patient = patientFeignClient.getPatientById(patientId);

        //3.请求第三方医院，确认当前用户能否挂号
        //因为第三方医院系统，并没有真正保存 就诊人的信息，所以只传 schedule信息即可，就诊人信息可传可不传
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",schedule.getHoscode());
        paramMap.put("depcode",schedule.getDepcode());
        paramMap.put("hosScheduleId",schedule.getHosScheduleId());
        paramMap.put("reserveDate",schedule.getReserveDate());
        paramMap.put("reserveTime",schedule.getReserveTime());
        paramMap.put("amount",schedule.getAmount());

        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");

        if (jsonObject != null && jsonObject.getInteger("code") == 200){
            JSONObject data = jsonObject.getJSONObject("data");
            //封装订单信息
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setUserId(patient.getUserId());
            //订单号，保证该订单号是唯一即可
            orderInfo.setOutTradeNo(System.currentTimeMillis() + "" + new Random().nextInt(100));

            //schedule的信息
            orderInfo.setAmount(schedule.getAmount());
            orderInfo.setHoscode(schedule.getHoscode());
            orderInfo.setHosname(schedule.getHosname());
            orderInfo.setDepcode(schedule.getDepcode());
            orderInfo.setDepname(schedule.getDepname());
            orderInfo.setTitle(schedule.getTitle());
            orderInfo.setReserveDate(schedule.getReserveDate());
            orderInfo.setReserveTime(schedule.getReserveTime());
            orderInfo.setQuitTime(schedule.getQuitTime());
            //医院排班id，排班编号（医院自己的排班主键）
            orderInfo.setScheduleId(schedule.getHosScheduleId());

            //patient的信息
            orderInfo.setPatientId(patient.getId());
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());

            //第三方医院信息
            orderInfo.setNumber(data.getInteger("number"));
            orderInfo.setHosRecordId(data.getString("hosRecordId"));
            orderInfo.setFetchTime(data.getString("fetchTime"));
            orderInfo.setFetchAddress(data.getString("fetchAddress"));

            //订单状态
            //使用枚举类，更加规范
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());

            //3.2 如果返回能挂号，就把医生排班信息，就诊人信息，第三方医院返回信息 添加到order_info表中
            baseMapper.insert(orderInfo);

            //3.3 更新平台上对应医生的剩余可预约数
                //平台上对应医生的剩余可预约数是由第三方医院提供的，因为线下也会消耗可预约数，需要由第三方医院确认可预约数
                //封装 发送到rabbitMQ里的消息 的pojo对象信息
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setAvailableNumber(data.getIntValue("availableNumber"));
            orderMqVo.setReservedNumber(data.getIntValue("reservedNumber"));

            //3.4 给就诊人发送短信提醒
                //封装要发送给就诊人的短信 信息
                //个人用户不能开通阿里云短信服务，所以短信功能没有用
                //以下代码只是做一个模拟
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(patient.getPhone());
            msmVo.setTemplateCode("您已经预约了上午${time}点的${name}医生的号，不要迟到！");
            Map<String, Object> map = new HashMap<>();
            map.put("time",schedule.getReserveTime());
            map.put("name",schedule.getTitle());
            msmVo.setParam(map);

            //将短信对象封装到 orderMqVo对象中一起发给 service_hosp，再由service_hosp发给service_sms
            orderMqVo.setMsmVo(msmVo);
            //调用 rabbitMQ工具类，生产者 发送消息给mq
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);


            //4.返回订单id，用于前端使用id再次请求后端，进行订单回显
            return orderInfo.getId();
        }else {
            //3.1 如果返回不能挂号，直接抛出异常
            throw new YyghException(20001,"号源已满");
        }
    }

    @Override
    public Page<OrderInfo> getOrderPage(Integer pageNum, Integer pageSize, OrderQueryVo queryVo) {
        Page<OrderInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",queryVo.getUserId());

        String outTradeNo = queryVo.getOutTradeNo(); //订单号
        String keyword = queryVo.getKeyword();  //医院名
        Long patientId = queryVo.getPatientId();  //就诊人id
        String orderStatus = queryVo.getOrderStatus();  //订单状态
        String reserveDate = queryVo.getReserveDate();  //预约日期
        String createTimeBegin = queryVo.getCreateTimeBegin();  //创建时间
        String createTimeEnd = queryVo.getCreateTimeEnd();//

        if (!StringUtils.isEmpty(outTradeNo)){
            queryWrapper.eq("out_trade_no",outTradeNo);
        }
        if (!StringUtils.isEmpty(keyword)){
            queryWrapper.like("hosname",keyword);
        }
        if (!StringUtils.isEmpty(patientId)){
            queryWrapper.eq("patient_id",patientId);
        }
        if (!StringUtils.isEmpty(orderStatus)){
            queryWrapper.eq("order_status",orderStatus);
        }
        if (!StringUtils.isEmpty(reserveDate)){
            queryWrapper.ge("reserve_date",reserveDate);
        }
        if (!StringUtils.isEmpty(createTimeBegin)){
            queryWrapper.ge("create_time",createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)){
            queryWrapper.le("update_time",createTimeEnd);
        }

        Page<OrderInfo> result = baseMapper.selectPage(page, queryWrapper);
        result.getRecords().stream().forEach(item->{
            this.packageOrderInfo(item);
        });

        return result;
    }

    @Override
    public OrderInfo detail(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        this.packageOrderInfo(orderInfo);
        return orderInfo;
    }

    @Override
    public void cancelOrder(Long orderId) {
        //1. 确定 当前取消预约时间 和 挂号订单对应订单表里的取消预约截止时间(order_info表里的quitTime) 比较
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        //当前时间是否已经超过 订单表里的取消预约截止时间
        //1.1 如果超过了，直接抛出异常，不让取消
        if (quitTime.isBeforeNow()){
            throw new YyghException(20001, "超过了退号的截止时间");
        }

        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        reqMap.put("sign", "");
        //2. 立即请求，平台请求第三方医院，确定一下用户是否可以取消，并且通知第三方医院该用户已取消
        JSONObject jsonObject = HttpRequestHelper.sendRequest(reqMap, "http://localhost:9998/order/updateCancelStatus");
        //2.1 第三方医院如果不同意取消，抛出异常，不能取消
        if (jsonObject == null || jsonObject.getInteger("code") != 200){
            throw new YyghException(20001, "第三方医院不同意取消");
        }

        //3. 判断用户是否对挂号订单是否已支付
        if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()){
            //3.1 如果已支付，要退款
            boolean flag = weiPayService.refund(orderId);
            if (!flag){
                throw new YyghException(20001,"退款失败");
            }
        }

        //4. 更新订单表的订单状态，支付记录表的支付状态
        orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
        baseMapper.updateById(orderInfo);

        UpdateWrapper<PaymentInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id",orderId);
        updateWrapper.set("payment_status", PaymentStatusEnum.REFUND.getStatus());
        paymentService.update(updateWrapper);

        //5. 更新医生的剩余可预约数信息，+1
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(orderInfo.getHosRecordId());
        //6. 给就诊人发送短信提示
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(orderInfo.getPatientPhone());
        orderMqVo.setMsmVo(msmVo);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);

    }

    @Override
    public void patientRemind() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));
        queryWrapper.ne("order_status",OrderStatusEnum.CANCLE.getStatus());
        List<OrderInfo> orderInfos = baseMapper.selectList(queryWrapper);

        for (OrderInfo orderInfo : orderInfos) {
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());

            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SMS,MqConst.ROUTING_SMS_ITEM,msmVo);
        }

    }

    @Override
    public Map<String, Object> statistics(OrderCountQueryVo orderCountQueryVo) {
        //涉及分组的sql语句，建议自己自定义，不要用baseMapper现成的
        List<OrderCountVo> queryVoList = baseMapper.statistics(orderCountQueryVo);

        //不能使用并行流进行操作，可能会导致两个list的数据不是一一对应的
        List<String> dateList = queryVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        List<Integer> countList = queryVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        Map<String,Object> map = new HashMap<>(2);
        map.put("dateList",dateList);
        map.put("countList",countList);
        return map;
    }

    public void packageOrderInfo(OrderInfo item){
        item.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(item.getOrderStatus()));
    }
}
