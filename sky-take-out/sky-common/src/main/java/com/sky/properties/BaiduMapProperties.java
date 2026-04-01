package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.baidu")
@Data
public class BaiduMapProperties {

    // 百度地图开放平台api: ak
    private String mapAk;
    // 百度地图开放平台api: sk
    private String mapSk;
    // 百度地图开放平台api: url
    private String mapUrl;

}
