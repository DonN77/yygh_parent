package com.donn.yygh.hosp.service;

import com.donn.yygh.model.hosp.Hospital;
import com.donn.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/26 11:30
 **/
public interface HospitalService {
    void saveHospital(Map<String, Object> map);

    String getSignWithHoscode(String hoscode);

    Hospital getHospitalByHoscode(String hoscode);

    Page<Hospital> getHospitalPage(Integer pageNum, Integer pageSize, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    Hospital detail(String id);

    List<Hospital> findByName(String name);

    Hospital getHospitalDetail(String hoscode);
}
