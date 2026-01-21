package com.weis.demo.config;

import com.weis.demo.agent.v2.*;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentV2Config {

    @Bean
    public IntentAgent intentAgent(ChatModel chatModel) {
        return AgenticServices.agentBuilder(IntentAgent.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public ImageIntentAgent imageIntentAgent(QwenChatModel chatModel) {
        return AgenticServices.agentBuilder(ImageIntentAgent.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public GuideAgent guideAgent(ChatModel chatModel) {
        return AgenticServices.agentBuilder(GuideAgent.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public RAGGuideAgent ragGuideAgent(ChatModel chatModel, RetrievalAugmentor retrievalAugmentorV2) {
        return AgenticServices.agentBuilder(RAGGuideAgent.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentorV2)
                .build();
    }

    @Bean
    public GuideFluxAgent guideFluxAgent(StreamingChatModel streamingChatModel) {
        return AiServices.builder(GuideFluxAgent.class)
                .streamingChatModel(streamingChatModel)
                .build();
    }

}
