package com.donn.yygh.sms.service.impl;

import com.donn.yygh.sms.service.SmsService;
import com.donn.yygh.sms.utils.HttpUtils;
import com.donn.yygh.sms.utils.RandomUtil;
import com.donn.yygh.vo.msm.MsmVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/4 22:01
 **/
@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean sendCode(String phone) {
        /*
            在实际开发中不会有下面if这一步，不会去redis中找是否存在
            因为在实际中，获取验证码的button按钮，一分钟点击一次，如果一分钟后没有填写验证码，再次点击按钮发起请求
            如果走下面的逻辑，是不会再收到验证码的，而是直接返回true
        */
        String redisCode = (String)redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(redisCode)){
            return true;
        }

        String host = "http://dingxin.market.alicloudapi.com";     //使用山东鼎信，这个也是固定的
        String path = "/dx/sendSms";   //固定的
        String method = "POST";     //要求使用post请求
        String appcode = "6d8d579bbfa74dd283362e5cad0219fa";    //阿里云购买相应服务之后，给的appcode

        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);

        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);

        String fourBitRandom = RandomUtil.getFourBitRandom();
        System.out.println(fourBitRandom);
        querys.put("param", "code:"+fourBitRandom);    //设置要发送的验证码

        querys.put("tpl_id", "TP1711063");    //固定的
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));

            //把验证码保存redis中一份
            //为了不耗费我山东鼎信发送验证码的次数，过期时间设置久一点，或者不过期，设置了30天过期
            redisTemplate.opsForValue().set(phone,fourBitRandom,30, TimeUnit.DAYS);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    //应该使用阿里云短信服务，进行发送短信
    //但是阿里云短信现在不支持个人用户，所以实现不了
    @Override
    public void sendMessage(MsmVo msmVo) {
        String phone = msmVo.getPhone();
        System.out.println("向"+phone+"发送信息");
    }
}
