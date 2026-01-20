package com.weis.demo.memory;

import com.weis.demo.dto.MemoryIdInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.weis.demo.memory.RedisChatMemoryStore.DEFAULT_TTL;

/**
 * MemoryId 创建器
 * 用于生成符合特定格式的 MemoryId
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryIdCreator {
    
    /**
     * MemoryId 类型常量
     */
    public static final int MEMBER_CONVERSATION_TYPE = 1; // 会员对话

    /**
     * Redis key前缀
     */
    private static final String MEMORY_ID_INFO_KEY_PREFIX = "ai:agent:memory-id-info:";

    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 通用的 MemoryId 创建方法
     * 
     * @param memoryIdInfo MemoryId信息DTO
     * @return 生成的 UUID
     */
    public String createMemoryId(MemoryIdInfoDTO memoryIdInfo) {
        if (memoryIdInfo == null) {
            throw new IllegalArgumentException("MemoryIdInfo 不能为空");
        }
        
        if (memoryIdInfo.getMemoryType() != MEMBER_CONVERSATION_TYPE) {
            throw new IllegalArgumentException("不支持的 MemoryId 类型: " + memoryIdInfo.getMemoryType());
        }
        
        // 参数验证
        if (memoryIdInfo.getStoreId() == null) {
            throw new IllegalArgumentException("StoreId 不能为空");
        }
        if (memoryIdInfo.getShopId() == null) {
            throw new IllegalArgumentException("ShopId 不能为空");
        }
        if (memoryIdInfo.getMemberId() == null) {
            throw new IllegalArgumentException("MemberId 不能为空");
        }

        // 生成UUID（去除横杠）
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        // 构建Redis key
        String redisKey = MEMORY_ID_INFO_KEY_PREFIX + uuid;
        
        // 将MemoryIdInfoDTO存入Redis，过期时间为1天（24小时 = 86400秒）
        redisTemplate.opsForValue().set(redisKey, memoryIdInfo, DEFAULT_TTL);
        
        return uuid;
    }
    
    /**
     * 解析 MemoryId，从 Redis 中获取对应的信息
     * 
     * @param memoryId UUID格式的MemoryId
     * @return MemoryIdInfo 对象，包含从Redis获取的信息
     */
    public MemoryIdInfoDTO parseMemoryId(String memoryId) {
        if (memoryId == null || memoryId.trim().isEmpty()) {
            log.warn("MemoryId 不能为空");
            return null;
        }
        
        // 构建Redis key
        String redisKey = MEMORY_ID_INFO_KEY_PREFIX + memoryId.trim();
        
        // 从Redis中获取MemoryIdInfoDTO
        MemoryIdInfoDTO memoryIdInfo = (MemoryIdInfoDTO) redisTemplate.opsForValue().get(redisKey);

        if (memoryIdInfo == null) {
            log.warn("未找到对应的 MemoryId 信息，可能已过期或不存在: " + memoryId);
        } else {
            redisTemplate.expire(redisKey, DEFAULT_TTL);
            log.debug("已刷新 MemoryId 过期时间: {}", memoryId);
        }
        return memoryIdInfo;
    }
    
}
