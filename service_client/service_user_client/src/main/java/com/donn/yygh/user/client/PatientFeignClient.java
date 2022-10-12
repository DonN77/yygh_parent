package com.donn.yygh.user.client;

import com.donn.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/9 11:22
 **/
@FeignClient(value = "service-user")
public interface PatientFeignClient {

    //根据就诊人id获取就诊人信息
    //使用openfeign远程调用，最好把 PathVariable的value带上，不然容易报错
    @GetMapping("/user/userInfo/patient/{patientId}")
    public Patient getPatientById(@PathVariable("patientId") Long patientId);

}
