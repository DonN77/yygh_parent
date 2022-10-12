package com.donn.yygh.hosp.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.common.utils.MD5;
import com.donn.yygh.hosp.service.HospitalSetService;
import com.donn.yygh.model.hosp.HospitalSet;
import com.donn.yygh.common.result.R;
import com.donn.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author donn
 * @since 2022-09-21
 */
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@Api(tags = "医院信息接口")
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

//    查找所有医院设置信息
    @GetMapping("/findAll")
    @ApiOperation(value = "查找所有医院设置信息")
    public R findAll(){
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("items",list);
    }

//    按照id删除医院设置信息
    @DeleteMapping("/deleteById/{id}")
    @ApiOperation(value = "按照id删除医院设置信息")
    public R deleteById(@PathVariable("id") Integer id){
        hospitalSetService.removeById(id);
        return R.ok();
    }

//    按照条件进行分页
    @PostMapping("/page/{pageNum}/{pageSize}")
    @ApiOperation(value = "按照条件进行分页")
    public R getPageInfo(@PathVariable Integer pageNum, @PathVariable Integer pageSize
                        ,@RequestBody HospitalSetQueryVo hospitalSetQueryVo){

        Page<HospitalSet> page = new Page<>(pageNum,pageSize);
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(hospitalSetQueryVo.getHoscode())){
            queryWrapper.eq("hoscode",hospitalSetQueryVo.getHoscode());
        }
        if(!StringUtils.isEmpty(hospitalSetQueryVo.getHosname())){
            queryWrapper.like("hosname",hospitalSetQueryVo.getHosname());
        }
        hospitalSetService.page(page, queryWrapper);
        return R.ok().data("total",page.getTotal()).data("rows",page.getRecords());
    }

//    新增医院设置信息
    @PostMapping("/saveHospSet")
    @ApiOperation(value = "新增医院设置信息")
    public R save(@RequestBody HospitalSet hospitalSet){
        hospitalSet.setStatus(1);
        Random random = new Random();
        hospitalSet.setSignKey( MD5.encrypt(System.currentTimeMillis()+ random.nextInt(1000)+""));
        hospitalSetService.save(hospitalSet);
        return R.ok();
    }

//    修改之回显数据
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Integer id){
        HospitalSet byId = hospitalSetService.getById(id);
        return R.ok().data("item",byId);
    }
//    修改之修改数据
    @PutMapping("/update")
    public R update(@RequestBody HospitalSet hospitalSet){
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

//    批量删除
    @DeleteMapping("/delete")
    public R batchDelete(@RequestBody List<Integer> ids){
        hospitalSetService.removeByIds(ids);
        return R.ok();
    }

//    锁定与解锁
    @PutMapping("/status/{id}/{status}")
    public R updateStatus(@PathVariable Long id,@PathVariable Integer status){
        HospitalSet hospitalSet = new HospitalSet();
        hospitalSet.setStatus(status);
        hospitalSet.setId(id);
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }
}

