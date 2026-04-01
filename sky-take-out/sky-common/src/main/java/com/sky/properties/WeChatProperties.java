package com.sky.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.wechat")
@Data
public class WeChatProperties {

    @Value("${sky.wechat.app-id}")
    private String appid; //小程序的appid
    @Value("${sky.wechat.app-secret}")
    private String secret; //小程序的秘钥

}
