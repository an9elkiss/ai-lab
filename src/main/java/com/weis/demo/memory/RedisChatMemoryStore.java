package com.weis.demo.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于Redis的聊天记忆存储实现
 * 使用RedisTemplate存储和管理聊天消息历史
 */
@Slf4j
@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Redis key前缀
     */
    private static final String CHAT_MEMORY_KEY_PREFIX = "ai:agent:memory:";

    /**
     * 默认过期时间（30分钟）
     */
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    /**
     * 获取指定记忆ID的聊天消息列表
     * 
     * @param memoryId 记忆ID
     * @return 聊天消息列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        if (memoryId == null) {
            log.error("Memory ID is null, returning empty message list");
            return new ArrayList<>();
        }

        try {
            String key = buildKey(memoryId);
            String messagesJson = redisTemplate.opsForValue().get(key);
            if (messagesJson == null) {
                log.warn("No messages found for memory ID: {}", memoryId);
                return new ArrayList<>();
            }

            List<ChatMessage> chatMessages = ChatMessageDeserializer.messagesFromJson(messagesJson);
            log.debug("Retrieved {} messages for memory ID: {}", chatMessages.size(), memoryId);
            return chatMessages;
        } catch (Exception e) {
            log.error("Failed to get messages for memory ID: {}", memoryId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新指定记忆ID的聊天消息列表
     * 
     * @param memoryId 记忆ID
     * @param messages 聊天消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (memoryId == null) {
            log.error("Memory ID is null, cannot update messages");
            return;
        }

        try {
            String key = buildKey(memoryId);
            
            if (CollectionUtils.isEmpty(messages)) {
                // 如果消息列表为空，删除该key
                redisTemplate.delete(key);
                log.warn("Deleted empty message list for memory ID: {}", memoryId);
                return;
            }

            String messagesJson = ChatMessageSerializer.messagesToJson(messages);
            // 存储到Redis并设置过期时间
            redisTemplate.opsForValue().set(key, messagesJson, DEFAULT_TTL);
            
            log.debug("Updated {} messages for memory ID: {}", messages.size(), memoryId);

        } catch (Exception e) {
            log.error("Failed to update messages for memory ID: {}", memoryId, e);
        }
    }

    /**
     * 删除指定记忆ID的聊天消息
     * 
     * @param memoryId 记忆ID
     */
    @Override
    public void deleteMessages(Object memoryId) {
        if (memoryId == null) {
            log.warn("Memory ID is null, cannot delete messages");
            return;
        }

        try {
            String key = buildKey(memoryId);
            Boolean deleted = redisTemplate.delete(key);
            
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Successfully deleted messages for memory ID: {}", memoryId);
            } else {
                log.debug("No messages found to delete for memory ID: {}", memoryId);
            }

        } catch (Exception e) {
            log.error("Failed to delete messages for memory ID: {}", memoryId, e);
        }
    }

    /**
     * 构建Redis key
     * 
     * @param memoryId 记忆ID
     * @return Redis key
     */
    private String buildKey(Object memoryId) {
        return CHAT_MEMORY_KEY_PREFIX + memoryId.toString();
    }

}
