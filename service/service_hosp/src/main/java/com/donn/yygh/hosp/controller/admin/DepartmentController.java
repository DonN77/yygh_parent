package com.donn.yygh.hosp.controller.admin;

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
 * @Date 2022/10/1 12:51
 **/
@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/{hoscode}")
    public R getDepartmentList(@PathVariable String hoscode){
        List<DepartmentVo> list = departmentService.getDepartmentList(hoscode);
        return R.ok().data("list",list);
    }

}
