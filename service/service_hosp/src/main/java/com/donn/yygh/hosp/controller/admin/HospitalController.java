package com.donn.yygh.hosp.controller.admin;

import com.donn.yygh.common.result.R;
import com.donn.yygh.hosp.bean.Result;
import com.donn.yygh.hosp.service.HospitalService;
import com.donn.yygh.model.hosp.Hospital;
import com.donn.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/28 10:39
 **/
@RestController
@RequestMapping("/admin/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @GetMapping("/{pageNum}/{pageSize}")
    public R getHospitalPage(@PathVariable Integer pageNum, @PathVariable Integer pageSize, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> page = hospitalService.getHospitalPage(pageNum,pageSize,hospitalQueryVo);
        return R.ok().data("total",page.getTotalElements()).data("list",page.getContent());
    }

    @PutMapping("/{id}/{status}")
    public R updateStatus(@PathVariable String id, @PathVariable Integer status){
        hospitalService.updateStatus(id,status);
        return R.ok();
    }

    @GetMapping("/detail/{id}")
    public R detail(@PathVariable String id){
        Hospital hospital = hospitalService.detail(id);
        return R.ok().data("hospital",hospital);
    }
}
