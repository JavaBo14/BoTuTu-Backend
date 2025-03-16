package com.bo.tutu.config;


import com.bo.tutu.utils.DateTimeUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

/**
 * Caffeine缓存配置类
 */
@Configuration
@Slf4j
public class CaffeineConfig {
    
    @Bean
    public Cache<String, Object> localCache() {
        Cache<String, Object> cache = Caffeine.newBuilder()
                .initialCapacity(1024)      // 初始容量
                .maximumSize(10_000L)       // 最大容量
                .expireAfterWrite(Duration.ofMinutes(5))  // 写入5分钟后过期
                .softValues()               // 使用软引用
                .recordStats()              // 开启统计
                .removalListener((key, value, cause) -> {
                    // 记录缓存移除日志
                    log.debug("本地缓存移除: key={}, cause={}, time={}", 
                            key, cause, DateTimeUtils.getCurrentTime());
                })
                .build();
        log.info("本地缓存初始化完成: time={}", DateTimeUtils.getCurrentTime());
        return cache;
    }
}