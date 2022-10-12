package com.donn.yygh.common.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/22 11:15
 **/
//设置mybatis-plus的分页插件,在配置类里 往容器注册PaginationInterceptor对象
//固定写法
@Configuration
public class PageConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
