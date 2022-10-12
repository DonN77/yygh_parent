package com.donn.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.donn.yygh.cmn.client.DictFeignClient;
import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.enums.DictEnum;
import com.donn.yygh.hosp.mapper.HospitalSetMapper;
import com.donn.yygh.hosp.repository.HospitalRepository;
import com.donn.yygh.hosp.service.HospitalService;
import com.donn.yygh.model.hosp.Hospital;
import com.donn.yygh.model.hosp.HospitalSet;
import com.donn.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/26 11:31
 **/
@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private HospitalSetMapper hospitalSetMapper;

    @Autowired
    private DictFeignClient dictFeignClient;

    //往mongodb里存放医院信息
    @Override
    public void saveHospital(Map<String, Object> map) {
        String s = JSONObject.toJSONString(map);
        Hospital hospital = JSONObject.parseObject(s, Hospital.class);

        Hospital find = hospitalRepository.findByHoscode(hospital.getHoscode());
        if(find==null){  //数据库中没有则进行增加
            hospital.setIsDeleted(0);
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }else{   //数据库中有则进行修改
            hospital.setId(find.getId());
            hospital.setStatus(find.getStatus());
            hospital.setIsDeleted(find.getIsDeleted());
            hospital.setCreateTime(find.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }

    }

    //通过hoscode查找密钥，一个hoscode对应一家医院，用于密钥验证
    @Override
    public String getSignWithHoscode(String hoscode) {
        QueryWrapper<HospitalSet>  queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = hospitalSetMapper.selectOne(queryWrapper);
        if (hospitalSet == null){
            throw new YyghException(20001,"该医院不存在");
        }else return hospitalSet.getSignKey();
    }

    //通过hoscode查找医院信息
    @Override
    public Hospital getHospitalByHoscode(String hoscode) {
        return hospitalRepository.findByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> getHospitalPage(Integer pageNum, Integer pageSize, HospitalQueryVo hospitalQueryVo) {
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        Pageable of = PageRequest.of(pageNum - 1, pageSize, Sort.by("hoscode").ascending());
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("hosname", ExampleMatcher.GenericPropertyMatchers.contains())  //name字段模糊匹配
                .withIgnoreCase(true);  //忽略大小写

        Example<Hospital> example = Example.of(hospital,matcher);
        Page<Hospital> result = hospitalRepository.findAll(example, of);
        result.getContent().stream().forEach(item ->{
            packageHospital(item);
        });

        return result;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        if(status == 1||status == 0){
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital detail(String id) {
        Hospital hospital = hospitalRepository.findById(id).get();
        packageHospital(hospital);
        return hospital;
    }

    @Override
    public List<Hospital> findByName(String name) {
//        Hospital hospital = new Hospital();
//        hospital.setHosname(name);
//        ExampleMatcher matcher = ExampleMatcher.matching()
//                .withMatcher("hosname", ExampleMatcher.GenericPropertyMatchers.contains())  //name字段模糊匹配
//                .withIgnoreCase(true);  //忽略大小写
//        Example<Hospital> example = Example.of(hospital,matcher);
//        List<Hospital> all = hospitalRepository.findAll(example);
//        return all;
        return hospitalRepository.findByHosnameLike(name);
    }

    @Override
    public Hospital getHospitalDetail(String hoscode) {
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        this.packageHospital(hospital);
        return hospital;
    }

    private void packageHospital(Hospital item) {
        String hostype = item.getHostype();
        String provinceCode = item.getProvinceCode();
        String cityCode = item.getCityCode();
        String districtCode = item.getDistrictCode();

        String provinceAddress = dictFeignClient.getNameByValue(Long.parseLong(provinceCode));
        String cityAddress = dictFeignClient.getNameByValue(Long.parseLong(cityCode));
        String districtAddress = dictFeignClient.getNameByValue(Long.parseLong(districtCode));

        String level = dictFeignClient.getNameByDictCodeAndValue(DictEnum.HOSTYPE.getDictCode(), Long.parseLong(hostype));

        item.getParam().put("hostypeString",level);
        item.getParam().put("fullAddress",provinceAddress+cityAddress+districtAddress+ item.getAddress());
    }
}
