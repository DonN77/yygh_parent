package com.donn.yygh.sms.service;

import com.donn.yygh.vo.msm.MsmVo;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/4 22:01
 **/
public interface SmsService {
    boolean sendCode(String phone);

    void sendMessage(MsmVo msmVo);
}
