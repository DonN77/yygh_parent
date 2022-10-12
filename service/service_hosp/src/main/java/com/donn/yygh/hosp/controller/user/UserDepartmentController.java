package com.donn.yygh.hosp.controller.user;

import com.donn.yygh.common.result.R;
import com.donn.yygh.hosp.service.DepartmentService;
import com.donn.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/3 14:38
 **/
@RestController
@RequestMapping("user/hosp/department")
public class UserDepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/all/{hoscode}")
    public R findAll(@PathVariable String hoscode){
        List<DepartmentVo> departmentList = departmentService.getDepartmentList(hoscode);
        return R.ok().data("list",departmentList);
    }
}
