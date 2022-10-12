package com.donn.yygh.common.config;

import com.google.common.base.Predicates;
import net.bytebuddy.dynamic.scaffold.MethodRegistry;
import org.reflections.util.FilterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.function.Predicate;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/21 23:17
 **/
@Configuration
@EnableSwagger2
//@EnableSwagger注解，表示支持开启swagger
//访问  /swagger-ui.html 页面，就可以使用swagger功能
public class SwaggerConfig {

    //往容器里注册Docket对象
    //一个分组对应一个Docket对象
    @Bean
    public Docket getAdminDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("admin")
                .apiInfo(getAdminApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/admin/.*")))    //设置该分组对应的请求路径
                .build();
    }
    public ApiInfo getAdminApiInfo(){
        return new ApiInfoBuilder()
                .title("管理员系统")
                .build();
    }

    @Bean
    public Docket getUserDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("user")
                .apiInfo(getUserApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/user/.*")))
                .build();
    }
    public ApiInfo getUserApiInfo(){
        return new ApiInfoBuilder()
                .title("用户系统")
                .build();
    }

    @Bean
    public Docket getApiDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("api")
                .apiInfo(getApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/api/.*")))
                .build();
    }
    public ApiInfo getApiInfo(){
        return new ApiInfoBuilder()
                .title("第三方医院系统")
                .build();
    }
}
