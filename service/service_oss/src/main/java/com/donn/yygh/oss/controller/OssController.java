package com.donn.yygh.oss.controller;

import com.donn.yygh.common.result.R;
import com.donn.yygh.oss.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/7 13:09
 **/
@RestController
@RequestMapping("/user/oss/file")
public class OssController {
    @Autowired
    private OssService ossService;

    @PostMapping("/upload")
    public R upload(MultipartFile file){
        String url = ossService.upload(file);
        return R.ok().data("url",url);
    }
}
