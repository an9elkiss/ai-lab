package com.weis.demo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Redis Sentinel配置类
 * 配置Redis Sentinel连接和RedisTemplate的序列化方式
 * 
 * @author weis
 * @since 2026-01-12
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisConfig {

    /**
     * Redis数据库索引
     */
    private int database = 0;

    /**
     * Redis密码
     */
    private String password;

    /**
     * Sentinel配置
     */
    private Sentinel sentinel = new Sentinel();

    /**
     * Sentinel配置内部类
     */
    @Data
    public static class Sentinel {
        /**
         * Sentinel主服务器名称
         */
        private String master;

        /**
         * Sentinel节点地址列表（逗号分隔格式）
         */
        private String nodes;

        /**
         * Sentinel密码
         */
        private String password;
    }

    /**
     * 配置Redis Sentinel连接工厂
     * 
     * @return LettuceConnectionFactory实例
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // 创建Sentinel配置
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        
        // 设置主服务器名称
        if (StringUtils.hasText(sentinel.getMaster())) {
            sentinelConfig.setMaster(sentinel.getMaster());
        } else {
            throw new IllegalArgumentException("Redis Sentinel master name cannot be empty");
        }
        
        // 设置数据库索引
        sentinelConfig.setDatabase(database);
        
        // 设置Redis密码
        if (StringUtils.hasText(password)) {
            sentinelConfig.setPassword(password);
        }
        
        // 设置Sentinel密码
        if (StringUtils.hasText(sentinel.getPassword())) {
            sentinelConfig.setSentinelPassword(sentinel.getPassword());
        }
        
        // 解析并添加Sentinel节点
        if (!StringUtils.hasText(sentinel.getNodes())) {
            throw new IllegalArgumentException("Redis Sentinel nodes cannot be empty");
        }
        
        Set<String> sentinelHostAndPorts = new HashSet<>();
        String[] nodes = sentinel.getNodes().split(",");
        for (String node : nodes) {
            String[] hostAndPort = node.trim().split(":");
            if (hostAndPort.length == 2) {
                sentinelHostAndPorts.add(node.trim());
                sentinelConfig.sentinel(hostAndPort[0].trim(), Integer.parseInt(hostAndPort[1].trim()));
            } else {
                log.warn("Invalid sentinel node format: {}, expected format: host:port", node);
            }
        }
        
        if (sentinelHostAndPorts.isEmpty()) {
            throw new IllegalArgumentException("No valid Redis Sentinel nodes found");
        }
        
        log.info("Redis Sentinel配置 - Master: {}, Nodes: {}, Database: {}", 
                sentinel.getMaster(), sentinelHostAndPorts, database);
        
        return new LettuceConnectionFactory(sentinelConfig);
    }

    /**
     * 配置RedisTemplate
     * 使用GenericJackson2JsonRedisSerializer进行序列化
     * 
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 StringRedisSerializer 来序列化和反序列化 redis 的 key
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 使用 GenericJackson2JsonRedisSerializer 来序列化和反序列化 redis 的 value
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        
        log.info("RedisTemplate配置完成，使用Sentinel模式连接，序列化器：GenericJackson2JsonRedisSerializer");
        return template;
    }
}