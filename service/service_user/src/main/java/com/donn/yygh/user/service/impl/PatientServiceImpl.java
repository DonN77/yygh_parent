package com.donn.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.donn.yygh.cmn.client.DictFeignClient;
import com.donn.yygh.common.utils.JwtHelper;
import com.donn.yygh.model.user.Patient;
import com.donn.yygh.user.mapper.PatientMapper;
import com.donn.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务实现类
 * </p>
 *
 * @author donn
 * @since 2022-10-07
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public List<Patient> findAll(String token) {
        Long userId = JwtHelper.getUserId(token);
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<Patient> patients = baseMapper.selectList(queryWrapper);
        patients.stream().forEach(item->{
            this.packagePatient(item);
        });
        return patients;
    }

    @Override
    public Patient detail(Long id) {
        Patient patient = baseMapper.selectById(id);
        this.packagePatient(patient);
        return patient;
    }

    @Override
    public List<Patient> selectList(QueryWrapper<Patient> queryWrapper) {
        List<Patient> patients = baseMapper.selectList(queryWrapper);
        patients.stream().forEach(this::packagePatient);
        return patients;
    }



    private void packagePatient(Patient item) {
        //远程调用cmn微服务，找 value 在字典中对应的名字
        item.getParam().put("certificatesTypeString", dictFeignClient.getNameByValue(Long.parseLong(item.getCertificatesType())));

        String provinceString = dictFeignClient.getNameByValue(Long.parseLong(item.getProvinceCode()));
        String cityString = dictFeignClient.getNameByValue(Long.parseLong(item.getCityCode()));
        String districtString = dictFeignClient.getNameByValue(Long.parseLong(item.getDistrictCode()));

        item.getParam().put("provinceString", provinceString);
        item.getParam().put("cityString", cityString);
        item.getParam().put("districtString", districtString);
        item.getParam().put("fullAddress",provinceString+cityString+districtString+item.getAddress());
    }
}
