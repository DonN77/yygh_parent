package com.donn.yygh.cmn.controller;


import com.alibaba.excel.EasyExcel;
import com.donn.yygh.cmn.service.DictService;
import com.donn.yygh.common.result.R;
import com.donn.yygh.model.cmn.Dict;
import com.donn.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 前端控制器
 * </p>
 *
 * @author donn
 * @since 2022-09-23
 */
@RestController
@RequestMapping("/admin/cmn")
public class DictController {
    @Autowired
    private DictService dictService;

    @GetMapping("/childList/{pid}")
    public R getChildListByPid(@PathVariable Long pid){
        List<Dict> list = dictService.getChildListByPid(pid);
        return R.ok().data("items",list);
    }

    //下载
    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        dictService.download(response);
    }

    //上传
    @PostMapping("/upload")
    public R upload(MultipartFile file) throws IOException {
        dictService.upload(file);
        return R.ok();
    }

    //该接口是提供其他微服务获取信息的，不需要封装成R对象返回，因为不是与前端交互
    //与微服务之间通信，@PathVariable 的value值要写上
    @GetMapping("/{value}")
    public String getNameByValue(@PathVariable("value") Long value){
        return dictService.getNameByValue(value);
    }

    @GetMapping("/{dictCode}/{value}")
    public String getNameByDictCodeAndValue(@PathVariable("dictCode") String dictCode,@PathVariable("value") Long value){
        return dictService.getNameByDictCodeAndValue(dictCode,value);
    }
}

