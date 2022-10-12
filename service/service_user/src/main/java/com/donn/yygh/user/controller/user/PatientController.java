package com.donn.yygh.user.controller.user;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.donn.yygh.common.result.R;
import com.donn.yygh.common.utils.JwtHelper;
import com.donn.yygh.model.user.Patient;
import com.donn.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author donn
 * @since 2022-10-07
 */
@RestController
@RequestMapping("/user/userInfo/patient")
public class PatientController {
    @Autowired
    private PatientService patientService;
    //增
    @PostMapping("/save")
    public R save(@RequestHeader String token, @RequestBody Patient patient){
        Long userId = JwtHelper.getUserId(token);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    //删
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id){
        patientService.removeById(id);
        return R.ok();
    }
    //1、修改之回显数据
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Long id){
        Patient patient = patientService.detail(id);
        return R.ok().data("patient",patient);
    }
    //2、修改之更新数据
    @PutMapping("/update")
    public R update(@RequestBody Patient patient){
        patientService.updateById(patient);
        return R.ok();
    }

    //查
    @GetMapping("/all")
    public R findAll(@RequestHeader String token){
        List<Patient> list = patientService.findAll(token);
        return R.ok().data("list",list);
    }

    //根据就诊人id获取就诊人信息
    //微服务之间远程调用的方法，就不用返回R对象了，需要什么返回什么就好了
    //使用openfeign远程调用，最好把 PathVariable的value带上，不然容易报错
    @GetMapping("/{patientId}")
    public Patient getPatientById(@PathVariable("patientId") Long patientId){
        return patientService.getById(patientId);
    }

}

