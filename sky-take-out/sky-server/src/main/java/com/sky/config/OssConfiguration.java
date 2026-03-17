package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfiguration {
    @Bean
    public AliOssUtil aliOssUtil(AliOssProperties p) {
        return new AliOssUtil(
                p.getEndpoint(),
                p.getAccessKeyId(),
                p.getAccessKeySecret(),
                p.getBucketName()
        );
    }
}