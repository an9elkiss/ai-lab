package com.weis.demo.config;

import com.weis.demo.memory.RedisChatMemoryStore;
import com.weis.demo.memory.SubMemoryIdRedisChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
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
public class ChatMemoryConfig {


    @Bean
    TokenCountEstimator tokenCountEstimator() {
        // DeepSeek 使用类似 GPT 的分词器，使用 GPT-3.5-turbo 的分词器作为兼容选项
        return new OpenAiTokenCountEstimator("gpt-3.5-turbo");
    }

    /**
     * 配置聊天记忆提供者
     * 使用Redis作为底层存储，支持Token窗口限制
     * 
     * @param tokenizer Token计数估算器
     * @return ChatMemoryProvider实例
     */
    @Bean
    ChatMemoryProvider chatMemoryProvider(TokenCountEstimator tokenizer, RedisChatMemoryStore redisChatMemoryStore) {
        return memoryId -> TokenWindowChatMemory.builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(memoryId)
                .maxTokens(5000, tokenizer)
                .build();
    }

    @Bean
    ChatMemoryProvider subMemoryIdChatMemoryProvider(TokenCountEstimator tokenizer, SubMemoryIdRedisChatMemoryStore redisChatMemoryStore) {
        return memoryId -> TokenWindowChatMemory.builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(memoryId)
                .maxTokens(5000, tokenizer)
                .build();
    }
}
