package com.donn.yygh.oss.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/7 12:37
 **/
@ConfigurationProperties(prefix = "oss.file")
@PropertySource(value = {"classpath:oss.properties"})
@Component
//去classpath:oss.yaml 文件中找以 oss.file开头的键，封装到该类的属性上，就可以不用全都写在 application.properties主配置文件中
//@PropertySource 不支持yaml文件，不能和@EnableConfigurationProperties搭配使用
@Data
public class OssProperties {
    private String bucketname;
    private String endpoint;
    private String keyid;
    private String keysecret;
}
