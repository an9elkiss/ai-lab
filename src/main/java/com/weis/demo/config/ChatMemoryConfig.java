package com.weis.demo.config;

import com.weis.demo.memory.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天记忆配置类
 * 配置基于Redis的聊天记忆存储
 * 
 * @author weis
 * @since 2026-01-12
 */
@Configuration
@RequiredArgsConstructor
public class ChatMemoryConfig {

    private final RedisChatMemoryStore redisChatMemoryStore;

    /**
     * 配置聊天记忆提供者
     * 使用Redis作为底层存储，支持Token窗口限制
     * 
     * @param tokenizer Token计数估算器
     * @return ChatMemoryProvider实例
     */
    @Bean
    ChatMemoryProvider chatMemoryProvider(TokenCountEstimator tokenizer) {
        return memoryId -> TokenWindowChatMemory.builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(memoryId)
                .maxTokens(5000, tokenizer)
                .build();
    }
}
