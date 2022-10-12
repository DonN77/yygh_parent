package com.donn.yygh.order.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/10 14:59
 **/
@PropertySource(value = "classpath:weipay.properties")
@ConfigurationProperties(prefix = "weipay")
@Component
@Data
public class WeiPayProperties {
    //关联的公众号appid
    private String appid;
    //商户号
    private String partner;
    //商户key
    private String partnerkey;
}
