package com.donn.yygh.hosp.controller.api;

import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.common.utils.MD5;
import com.donn.yygh.hosp.bean.Result;
import com.donn.yygh.hosp.service.HospitalService;
import com.donn.yygh.hosp.utils.HttpRequestHelper;
import com.donn.yygh.model.hosp.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/26 11:07
 **/
@RestController
@RequestMapping("/api/hosp")
public class ApiHospitalController {

    @Autowired
    private HospitalService hospitalService;

//    第三方医院系统 新增医院信息
    @PostMapping("/saveHospital")
//    因为 hospital-manage 那边是通过byte字节流进行传输数据的，所以使用HttpServletRequest进行获取
    public Result saveHospital(HttpServletRequest request){
        Map<String,Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String requestSign =(String) map.get("sign");
        String hoscode = (String) map.get("hoscode");
        String signKey = hospitalService.getSignWithHoscode(hoscode);
        String encrypt = MD5.encrypt(signKey);
        //判断第三方医院传过来的密钥，与保存在服务器的密钥是否一致，一致才可以操作
        //密钥验证
        if (!StringUtils.isEmpty(requestSign) && !StringUtils.isEmpty(signKey) && encrypt.equals(requestSign)){
            String logoData =(String) map.get("logoData");
//            因为在数据传输过程中，第三方医院使用流的方式进行传输，+号会变成空格，需要还原
            String s = logoData.replaceAll(" ", "+");
            map.put("logoData",s);
            hospitalService.saveHospital(map);
            return Result.ok();
        }else throw new YyghException(20001,"保存失败");

    }

    //获取医院信息
    @PostMapping("/hospital/show")
    public Result<Hospital> getHospitalInfo(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode =(String) map.get("hoscode");
        String requestSign = (String) map.get("sign");
        String signKey = hospitalService.getSignWithHoscode(hoscode);
        String encrypt = MD5.encrypt(signKey);
        //判断第三方医院传过来的密钥，与保存在服务器的密钥是否一致，一致才可以操作
        //密钥验证
        if (!StringUtils.isEmpty(requestSign) && !StringUtils.isEmpty(signKey) && encrypt.equals(requestSign)){
            Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
            return Result.ok(hospital);
        }else{
            throw new YyghException(20001,"展示失败");
        }
    }
}
