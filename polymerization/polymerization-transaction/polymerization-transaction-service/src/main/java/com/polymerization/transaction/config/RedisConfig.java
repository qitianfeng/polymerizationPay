package com.polymerization.transaction.config;

import com.polymerization.common.cache.Cache;
import com.polymerization.transaction.common.util.RedisCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
    @Bean
    public Cache cache(StringRedisTemplate redisTemplate){
        return new RedisCache(redisTemplate);
    }
}
