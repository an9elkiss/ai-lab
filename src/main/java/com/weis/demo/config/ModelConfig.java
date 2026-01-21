package com.weis.demo.config;

import com.weis.demo.agent.listener.ChatModelListenerImpl;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.http.client.HttpClientBuilderFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ModelConfig {

    @Bean
    ChatModelListener chatModelListener() {
        // 注册自定义的聊天模型监听器，用于监控和记录所有聊天模型交互
        return new ChatModelListenerImpl();
    }

    @Bean
    public QwenChatModel qwenChatModel(
            @Value("${langchain4j.dashscope.chat-model.api-key}") String apiKey,
            @Value("${langchain4j.dashscope.chat-model.model-name}") String modelName,
            @Value("${langchain4j.dashscope.chat-model.temperature}") Float temperature,
            ChatModelListener chatModelListener) {

        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .listeners(List.of(chatModelListener))
                .build();
    }

    @Bean
    public ChatModel chatModel(
            @Value("${langchain4j.open-ai.chat-model.api-key}") String apiKey,
            @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName,
            @Value("${langchain4j.open-ai.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.open-ai.chat-model.temperature}") Double temperature,
            HttpClientBuilderFactory httpClientBuilderFactory,
            ChatModelListener chatModelListener) {


        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(temperature)
                .listeners(List.of(chatModelListener))
                .httpClientBuilder(httpClientBuilderFactory.create())
                .strictJsonSchema(true)
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel(
            @Value("${langchain4j.open-ai.chat-model.api-key}") String apiKey,
            @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName,
            @Value("${langchain4j.open-ai.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.open-ai.chat-model.temperature}") Double temperature,
            HttpClientBuilderFactory httpClientBuilderFactory,
            ChatModelListener chatModelListener) {

        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(temperature)
                .listeners(List.of(chatModelListener))
                .httpClientBuilder(httpClientBuilderFactory.create())
                .build();
    }
}
