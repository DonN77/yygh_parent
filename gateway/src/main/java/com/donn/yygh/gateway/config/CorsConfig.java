package com.donn.yygh.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/30 21:07
 **/
@Configuration
//统一跨域处理
public class CorsConfig {
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        //一般这三个都是*
        //设置允许通过gateway的请求方式，*表示所有都允许
        config.addAllowedMethod("*");
        //设置能访问网关 的ip
        config.addAllowedOrigin("*");
        //允许携带各种请求头来访问 网关
        // config.addAllowedHeader("token") 表示请求头需要带有 token的键值对才能访问
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}

