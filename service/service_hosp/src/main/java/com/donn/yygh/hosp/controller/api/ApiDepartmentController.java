package com.donn.yygh.hosp.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.donn.yygh.hosp.bean.Result;
import com.donn.yygh.hosp.service.DepartmentService;
import com.donn.yygh.hosp.utils.HttpRequestHelper;
import com.donn.yygh.model.hosp.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/26 21:25
 **/
@RestController
@RequestMapping("/api/hosp")
//对应科室对应请求
public class ApiDepartmentController {

    @Autowired
    private DepartmentService departmentService;

    //新增科室
    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
//        验证密钥
        departmentService.saveDepartment(map);
        return Result.ok();
    }

    //分页查询科室，需要返回 Page对象
    @PostMapping("/department/list")
    public Result<Page> getDepartmentPage(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        Page<Department> page = departmentService.getDepartmentPage(map);
        return Result.ok(page);
    }

    @PostMapping("/department/remove")
    public Result removeDepartment(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        departmentService.removeDepartment(map);
        return Result.ok();
    }
}
