package com.donn.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.donn.yygh.hosp.repository.DepartmentRepository;
import com.donn.yygh.hosp.service.DepartmentService;
import com.donn.yygh.model.hosp.Department;
import com.donn.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/26 21:47
 **/
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;


    @Override
    public void saveDepartment(Map<String, Object> map) {
        Department department = JSONObject.parseObject(JSONObject.toJSONString(map), Department.class);

        //保存在yygh平台数据库的department
        Department platformDepartment = departmentRepository.findByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());
        if (platformDepartment == null){   //无则新增
            department.setUpdateTime(new Date());
            department.setCreateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }else{   //有则修改
            department.setId(platformDepartment.getId());
            department.setUpdateTime(new Date());
            department.setCreateTime(platformDepartment.getCreateTime());
            department.setIsDeleted(platformDepartment.getIsDeleted());
            departmentRepository.save(department);
        }
    }

    //科室分页查询
    @Override
    public Page<Department> getDepartmentPage(Map<String, Object> map) {
        //通过hoscode，查询该医院下所有科室
        String hoscode = (String) map.get("hoscode");
        int page = Integer.parseInt((String) map.get("page"));
        int limit = Integer.parseInt((String) map.get("limit"));

        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        //因为 departmentRepository 的分页，0对应是第一页，所以需要 -1
        Pageable pageable = PageRequest.of(page-1,limit);
        return departmentRepository.findAll(example, pageable);
    }

    @Override
    public void removeDepartment(Map<String, Object> map) {
        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
//        因为departmentRepository的delete方法，本质上都是基于主键 _id删除的
//        所以需要先通过 hoscode和depcode 查出来，该医院科室对应的 _id值，再进行删除
        if (department != null){
            departmentRepository.deleteById(department.getId());
        }
    }

    @Override
    public List<DepartmentVo> getDepartmentList(String hoscode) {
        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        List<Department> all = departmentRepository.findAll(example);

        //使用流的方式进行分组，通过bigcode进行分组
        Map<String, List<Department>> collect = all.stream().collect(Collectors.groupingBy(Department::getBigcode));

        List<DepartmentVo> bigDepartmentList = new ArrayList<>();

        for (Map.Entry<String, List<Department>> entry : collect.entrySet()) {
            String bigcode = entry.getKey();
            //bigcode下的所有小科室
            List<Department> childList = entry.getValue();

            DepartmentVo bigDepartmentVo = new DepartmentVo();
            //设置大科室的code
            bigDepartmentVo.setDepcode(bigcode);
            //设置大科室的名字
            bigDepartmentVo.setDepname(childList.get(0).getBigname());

            //遍历小科室，设置大科室下的小科室信息
            List<DepartmentVo> smallDepartmentList = new ArrayList<>();
            for (Department department1 : childList) {
                DepartmentVo smallDepartment = new DepartmentVo();
                //设置当前子科室的名字
                smallDepartment.setDepname(department1.getDepname());
                //设置当前子科室的code
                smallDepartment.setDepcode(department1.getDepcode());
                smallDepartmentList.add(smallDepartment);
            }
            bigDepartmentVo.setChildren(smallDepartmentList);
            bigDepartmentList.add(bigDepartmentVo);
        }

        return bigDepartmentList;
    }
}
