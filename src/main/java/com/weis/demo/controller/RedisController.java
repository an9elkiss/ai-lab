package com.weis.demo.controller;

import com.weis.demo.service.RedisService;
import com.weis.demo.service.RedisSentinelHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis操作控制器
 * 提供Redis基本操作的REST接口
 * 
 * @author weis
 * @since 2026-01-12
 */
@Slf4j
@RestController
@RequestMapping("/redis")
@Tag(name = "Redis操作", description = "Redis缓存操作相关接口")
public class RedisController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisSentinelHealthService redisSentinelHealthService;

    // ============================String操作=============================
    
    @Operation(summary = "设置字符串值", description = "向Redis中设置一个字符串键值对")
    @PostMapping("/string/set")
    public Map<String, Object> setString(
            @Parameter(description = "键名") @RequestParam String key,
            @Parameter(description = "值") @RequestParam String value,
            @Parameter(description = "过期时间(秒)，可选") @RequestParam(required = false) Long expireTime) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success;
            if (expireTime != null && expireTime > 0) {
                success = redisService.set(key, value, expireTime);
            } else {
                success = redisService.set(key, value);
            }
            
            result.put("success", success);
            result.put("message", success ? "设置成功" : "设置失败");
            result.put("key", key);
            result.put("value", value);
            if (expireTime != null) {
                result.put("expireTime", expireTime);
            }
        } catch (Exception e) {
            log.error("设置字符串失败", e);
            result.put("success", false);
            result.put("message", "设置失败: " + e.getMessage());
        }
        return result;
    }

    @Operation(summary = "获取字符串值", description = "根据键名获取Redis中的字符串值")
    @GetMapping("/string/get")
    public Map<String, Object> getString(@Parameter(description = "键名") @RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            Object value = redisService.get(key);
            result.put("success", true);
            result.put("key", key);
            result.put("value", value);
            result.put("exists", value != null);
        } catch (Exception e) {
            log.error("获取字符串失败", e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    @Operation(summary = "删除键", description = "删除Redis中的一个或多个键")
    @DeleteMapping("/key/delete")
    public Map<String, Object> deleteKey(@Parameter(description = "键名，多个用逗号分隔") @RequestParam String keys) {
        Map<String, Object> result = new HashMap<>();
        try {
            String[] keyArray = keys.split(",");
            redisService.del(keyArray);
            result.put("success", true);
            result.put("message", "删除成功");
            result.put("deletedKeys", keyArray);
        } catch (Exception e) {
            log.error("删除键失败", e);
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    @Operation(summary = "检查键是否存在", description = "检查Redis中是否存在指定的键")
    @GetMapping("/key/exists")
    public Map<String, Object> hasKey(@Parameter(description = "键名") @RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean exists = redisService.hasKey(key);
            result.put("success", true);
            result.put("key", key);
            result.put("exists", exists);
        } catch (Exception e) {
            log.error("检查键是否存在失败", e);
            result.put("success", false);
            result.put("message", "检查失败: " + e.getMessage());
        }
        return result;
    }

    @Operation(summary = "设置键的过期时间", description = "为指定的键设置过期时间")
    @PostMapping("/key/expire")
    public Map<String, Object> setExpire(
            @Parameter(description = "键名") @RequestParam String key,
            @Parameter(description = "过期时间(秒)") @RequestParam long expireTime) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = redisService.expire(key, expireTime);
            result.put("success", success);
            result.put("message", success ? "设置过期时间成功" : "设置过期时间失败");
            result.put("key", key);
            result.put("expireTime", expireTime);
        } catch (Exception e) {
            log.error("设置过期时间失败", e);
            result.put("success", false);
            result.put("message", "设置失败: " + e.getMessage());
        }
        return result;
    }

    // ============================Hash操作=============================
    
    @Operation(summary = "设置Hash值", description = "向Redis Hash中设置一个字段值")
    @PostMapping("/hash/set")
    public Map<String, Object> setHash(
            @Parameter(description = "Hash键名") @RequestParam String key,
            @Parameter(description = "字段名") @RequestParam String field,
            @Parameter(description = "字段值") @RequestParam String value) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = redisService.hset(key, field, value);
            result.put("success", success);
            result.put("message", success ? "设置Hash成功" : "设置Hash失败");
            result.put("key", key);
            result.put("field", field);
            result.put("value", value);
        } catch (Exception e) {
            log.error("设置Hash失败", e);
            result.put("success", false);
            result.put("message", "设置失败: " + e.getMessage());
        }
        return result;
    }

    @Operation(summary = "获取Hash值", description = "从Redis Hash中获取指定字段的值")
    @GetMapping("/hash/get")
    public Map<String, Object> getHash(
            @Parameter(description = "Hash键名") @RequestParam String key,
            @Parameter(description = "字段名") @RequestParam String field) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            Object value = redisService.hget(key, field);
            result.put("success", true);
            result.put("key", key);
            result.put("field", field);
            result.put("value", value);
            result.put("exists", value != null);
        } catch (Exception e) {
            log.error("获取Hash失败", e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    @Operation(summary = "获取Hash所有字段", description = "获取Redis Hash中的所有字段和值")
    @GetMapping("/hash/getAll")
    public Map<String, Object> getAllHash(@Parameter(description = "Hash键名") @RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<Object, Object> hashData = redisService.hmget(key);
            result.put("success", true);
            result.put("key", key);
            result.put("data", hashData);
            result.put("size", hashData.size());
        } catch (Exception e) {
            log.error("获取Hash所有字段失败", e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    // ============================List操作=============================
    
    @Operation(summary = "向List添加元素", description = "向Redis List的右端添加一个元素")
    @PostMapping("/list/push")
    public Map<String, Object> pushList(
            @Parameter(description = "List键名") @RequestParam String key,
            @Parameter(description = "元素值") @RequestParam String value) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = redisService.lSet(key, value);
            result.put("success", success);
            result.put("message", success ? "添加元素成功" : "添加元素失败");
            result.put("key", key);
            result.put("value", value);
        } catch (Exception e) {
            log.error("向List添加元素失败", e);
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
        }
        return result;
    }

    @Operation(summary = "获取List元素", description = "获取Redis List中指定范围的元素")
    @GetMapping("/list/range")
    public Map<String, Object> getListRange(
            @Parameter(description = "List键名") @RequestParam String key,
            @Parameter(description = "开始索引") @RequestParam(defaultValue = "0") long start,
            @Parameter(description = "结束索引") @RequestParam(defaultValue = "-1") long end) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            List<Object> list = redisService.lGet(key, start, end);
            result.put("success", true);
            result.put("key", key);
            result.put("data", list);
            result.put("size", list != null ? list.size() : 0);
        } catch (Exception e) {
            log.error("获取List元素失败", e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    // ============================Set操作=============================
    
    @Operation(summary = "向Set添加元素", description = "向Redis Set中添加一个或多个元素")
    @PostMapping("/set/add")
    public Map<String, Object> addSet(
            @Parameter(description = "Set键名") @RequestParam String key,
            @Parameter(description = "元素值，多个用逗号分隔") @RequestParam String values) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            String[] valueArray = values.split(",");
            long count = redisService.sSet(key, (Object[]) valueArray);
            result.put("success", true);
            result.put("message", "添加元素成功");
            result.put("key", key);
            result.put("addedCount", count);
            result.put("values", valueArray);
        } catch (Exception e) {
            log.error("向Set添加元素失败", e);
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
        }
        return result;
    }

    @Operation(summary = "获取Set所有元素", description = "获取Redis Set中的所有元素")
    @GetMapping("/set/members")
    public Map<String, Object> getSetMembers(@Parameter(description = "Set键名") @RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            Set<Object> members = redisService.sGet(key);
            result.put("success", true);
            result.put("key", key);
            result.put("members", members);
            result.put("size", members != null ? members.size() : 0);
        } catch (Exception e) {
            log.error("获取Set元素失败", e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    @Operation(summary = "测试Redis连接", description = "测试Redis连接是否正常")
    @GetMapping("/test")
    public Map<String, Object> testRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            String testKey = "test:connection:" + System.currentTimeMillis();
            String testValue = "Redis Sentinel连接测试";
            
            // 测试设置值
            boolean setResult = redisService.set(testKey, testValue, 60);
            if (!setResult) {
                result.put("success", false);
                result.put("message", "设置测试值失败");
                return result;
            }
            
            // 测试获取值
            Object getValue = redisService.get(testKey);
            if (!testValue.equals(getValue)) {
                result.put("success", false);
                result.put("message", "获取测试值失败");
                return result;
            }
            
            // 测试删除值
            redisService.del(testKey);
            
            result.put("success", true);
            result.put("message", "Redis Sentinel连接正常");
            result.put("testKey", testKey);
            result.put("testValue", testValue);
            result.put("mode", "Sentinel");
            
        } catch (Exception e) {
            log.error("Redis Sentinel连接测试失败", e);
            result.put("success", false);
            result.put("message", "Redis Sentinel连接异常: " + e.getMessage());
        }
        return result;
    }

    // ============================Sentinel监控=============================
    
    @Operation(summary = "获取Sentinel健康状态", description = "获取Redis Sentinel集群的健康状态信息")
    @GetMapping("/sentinel/health")
    public Map<String, Object> getSentinelHealth() {
        try {
            Map<String, Object> healthStatus = redisSentinelHealthService.getHealthStatus();
            healthStatus.put("timestamp", System.currentTimeMillis());
            return healthStatus;
        } catch (Exception e) {
            log.error("获取Sentinel健康状态失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("healthy", false);
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
    }

    @Operation(summary = "获取主节点信息", description = "获取Redis Sentinel当前主节点的信息")
    @GetMapping("/sentinel/master")
    public Map<String, Object> getMasterInfo() {
        try {
            Map<String, Object> masterInfo = redisSentinelHealthService.getMasterInfo();
            masterInfo.put("timestamp", System.currentTimeMillis());
            return masterInfo;
        } catch (Exception e) {
            log.error("获取主节点信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
    }

    @Operation(summary = "Sentinel综合状态", description = "获取Redis Sentinel的综合状态信息")
    @GetMapping("/sentinel/status")
    public Map<String, Object> getSentinelStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            // 获取健康状态
            Map<String, Object> healthStatus = redisSentinelHealthService.getHealthStatus();
            status.put("health", healthStatus);
            
            // 获取主节点信息
            Map<String, Object> masterInfo = redisSentinelHealthService.getMasterInfo();
            status.put("master", masterInfo);
            
            // 执行连接测试
            String testKey = "test:sentinel:status:" + System.currentTimeMillis();
            String testValue = "Sentinel状态检查";
            boolean testResult = redisService.set(testKey, testValue, 30);
            if (testResult) {
                Object getValue = redisService.get(testKey);
                redisService.del(testKey);
                status.put("connectionTest", Map.of(
                    "success", testValue.equals(getValue),
                    "message", "连接测试通过"
                ));
            } else {
                status.put("connectionTest", Map.of(
                    "success", false,
                    "message", "连接测试失败"
                ));
            }
            
            status.put("timestamp", System.currentTimeMillis());
            status.put("overall", healthStatus.get("healthy"));
            
        } catch (Exception e) {
            log.error("获取Sentinel综合状态失败", e);
            status.put("error", e.getMessage());
            status.put("overall", false);
            status.put("timestamp", System.currentTimeMillis());
        }
        return status;
    }
}