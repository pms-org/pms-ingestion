package com.pms.ingestion.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisLettuceConfig {
    
    @Value("${spring.data.redis.host}")
    private String redisHost;


    @Value("${spring.data.redis.port}")
    private int redisPort;


    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        String uri = String.format("redis://%s:%d", redisHost, redisPort);
        return RedisClient.create(uri);
    }


    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> connection(RedisClient redisClient) {
        return redisClient.connect();
    }
}

