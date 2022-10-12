package com.donn.yygh.sms.controller;

import com.donn.yygh.common.result.R;
import com.donn.yygh.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/4 22:00
 **/
@RestController
@RequestMapping("/user/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @PostMapping("send/{phone}")
    public R sendCode(@PathVariable String phone){
        boolean flag = smsService.sendCode(phone);
        if (flag){
            return R.ok();
        }else {
            return R.error();
        }
    }
}
