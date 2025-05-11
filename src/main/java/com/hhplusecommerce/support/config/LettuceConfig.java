package com.hhplusecommerce.support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class LettuceConfig {

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory(Environment env) {
        String host = env.getProperty("spring.redis.host");
        int port = Integer.parseInt(env.getProperty("spring.redis.port", "6379"));
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config);
    }
}
