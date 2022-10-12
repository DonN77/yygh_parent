package com.donn.yygh.hosp.service;

import com.donn.yygh.model.hosp.Department;
import com.donn.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/26 21:46
 **/
public interface DepartmentService {

    void saveDepartment(Map<String, Object> map);

    Page<Department> getDepartmentPage(Map<String, Object> map);

    void removeDepartment(Map<String, Object> map);

    List<DepartmentVo> getDepartmentList(String hoscode);
}
