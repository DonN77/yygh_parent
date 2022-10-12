package com.donn.yygh.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donn.yygh.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author donn
 * @since 2022-10-07
 */
public interface PatientService extends IService<Patient> {

    List<Patient> findAll(String token);

    Patient detail(Long id);

    List<Patient> selectList(QueryWrapper<Patient> queryWrapper);

}
