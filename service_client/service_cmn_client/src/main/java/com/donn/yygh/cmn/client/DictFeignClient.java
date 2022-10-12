package com.donn.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/28 17:00
 **/
@FeignClient(value = "service-cmn")   //被调用方在注册中心的名字，就是在配置文件中的spring.application.name的值
public interface DictFeignClient {
    
    @GetMapping("/admin/cmn/{value}")
    public String getNameByValue(@PathVariable("value") Long value);

    @GetMapping("/admin/cmn/{dictCode}/{value}")
    public String getNameByDictCodeAndValue(@PathVariable("dictCode") String dictCode,@PathVariable("value") Long value);
}
