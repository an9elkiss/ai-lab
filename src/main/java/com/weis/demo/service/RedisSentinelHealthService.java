package com.weis.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis Sentinel健康检查服务
 * 提供Redis Sentinel集群状态监控功能
 * 
 * @author weis
 * @since 2026-01-12
 */
@Slf4j
@Service
public class RedisSentinelHealthService {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${spring.data.redis.sentinel.master:mymaster}")
    private String masterName;

    @Value("${spring.data.redis.sentinel.nodes:localhost:26379,localhost:26380,localhost:26381}")
    private String sentinelNodes;

    /**
     * 获取Redis Sentinel集群健康状态
     * 
     * @return 健康状态信息
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> healthInfo = new HashMap<>();
        
        try {
            // 获取连接工厂信息
            if (redisConnectionFactory instanceof LettuceConnectionFactory) {
                LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) redisConnectionFactory;
                
                healthInfo.put("connectionFactoryType", "LettuceConnectionFactory");
                healthInfo.put("masterName", masterName);
                healthInfo.put("sentinelNodes", sentinelNodes.split(","));
                healthInfo.put("database", lettuceFactory.getDatabase());
                
                // 测试连接
                try {
                    lettuceFactory.getConnection().ping();
                    healthInfo.put("connectionStatus", "CONNECTED");
                    healthInfo.put("healthy", true);
                    log.info("Redis Sentinel连接正常");
                } catch (Exception e) {
                    healthInfo.put("connectionStatus", "DISCONNECTED");
                    healthInfo.put("healthy", false);
                    healthInfo.put("error", e.getMessage());
                    log.warn("Redis Sentinel连接异常: {}", e.getMessage());
                }
            } else {
                healthInfo.put("connectionFactoryType", redisConnectionFactory.getClass().getSimpleName());
                healthInfo.put("healthy", false);
                healthInfo.put("error", "不是Sentinel连接工厂");
            }
            
        } catch (Exception e) {
            log.error("获取Redis Sentinel健康状态失败", e);
            healthInfo.put("healthy", false);
            healthInfo.put("error", e.getMessage());
        }
        
        return healthInfo;
    }

    /**
     * 获取当前主节点信息
     * 
     * @return 主节点信息
     */
    public Map<String, Object> getMasterInfo() {
        Map<String, Object> masterInfo = new HashMap<>();
        
        try {
            if (redisConnectionFactory instanceof LettuceConnectionFactory) {
                LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) redisConnectionFactory;
                
                masterInfo.put("masterName", masterName);
                masterInfo.put("database", lettuceFactory.getDatabase());
                masterInfo.put("configured", true);
                
                // 尝试获取连接来验证主节点
                try {
                    lettuceFactory.getConnection().ping();
                    masterInfo.put("accessible", true);
                    log.debug("Redis主节点可访问");
                } catch (Exception e) {
                    masterInfo.put("accessible", false);
                    masterInfo.put("error", e.getMessage());
                    log.warn("Redis主节点不可访问: {}", e.getMessage());
                }
            } else {
                masterInfo.put("configured", false);
                masterInfo.put("error", "未配置Sentinel模式");
            }
            
        } catch (Exception e) {
            log.error("获取Redis主节点信息失败", e);
            masterInfo.put("error", e.getMessage());
        }
        
        return masterInfo;
    }
}