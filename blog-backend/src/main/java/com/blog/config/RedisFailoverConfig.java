package com.blog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisFailoverConfig {

    @PostConstruct
    public void init() {
        log.info("Redis配置已加载，如连接失败将使用内存缓存");
    }
}
