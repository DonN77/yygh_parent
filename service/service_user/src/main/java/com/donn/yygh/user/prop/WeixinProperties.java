package com.donn.yygh.user.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/5 23:28
 **/
@ConfigurationProperties(prefix = "weixin")
@Data
public class WeixinProperties {

    private String appid;
    private String appsecret;
    private String redirecturl;
}
