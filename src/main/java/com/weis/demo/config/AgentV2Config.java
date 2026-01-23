package com.weis.demo.config;

import com.weis.demo.agent.provider.SubMemoryIdSystemMessageProvider;
import com.weis.demo.agent.v2.*;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentV2Config {

    @Bean
    public IntentAgent intentAgent(ChatModel chatModel,
                                   SubMemoryIdSystemMessageProvider systemMessageProvider,
                                   ChatMemoryProvider subMemoryIdChatMemoryProvider) {
        return AgenticServices.agentBuilder(IntentAgent.class)
                .chatModel(chatModel)
                .systemMessageProvider(systemMessageProvider)
                .chatMemoryProvider(subMemoryIdChatMemoryProvider)
                .build();
    }

    @Bean
    public ImageIntentAgent imageIntentAgent(QwenChatModel chatModel,
                                             SubMemoryIdSystemMessageProvider systemMessageProvider,
                                             ChatMemoryProvider subMemoryIdChatMemoryProvider) {
        return AgenticServices.agentBuilder(ImageIntentAgent.class)
                .chatModel(chatModel)
                // 没有记忆拿不到SystemMessage，所以写死
//                .systemMessageProvider(systemMessageProvider)
                // OpenAiTokenCountEstimator 不支持 ImageContent，所以无法添加记忆
//                .chatMemoryProvider(subMemoryIdChatMemoryProvider)
                .build();
    }

    @Bean
    public GuideAgent guideAgent(ChatModel chatModel,
                                 SubMemoryIdSystemMessageProvider systemMessageProvider,
                                 ChatMemoryProvider subMemoryIdChatMemoryProvider) {
        return AgenticServices.agentBuilder(GuideAgent.class)
                .chatModel(chatModel)
                .systemMessageProvider(systemMessageProvider)
                .chatMemoryProvider(subMemoryIdChatMemoryProvider)
                .build();
    }

    @Bean
    public RAGGuideAgent ragGuideAgent(ChatModel chatModel,
                                       RetrievalAugmentor retrievalAugmentorV2,
                                       SubMemoryIdSystemMessageProvider systemMessageProvider,
                                       ChatMemoryProvider subMemoryIdChatMemoryProvider) {
        return AgenticServices.agentBuilder(RAGGuideAgent.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentorV2)
                .systemMessageProvider(systemMessageProvider)
                .chatMemoryProvider(subMemoryIdChatMemoryProvider)
                .build();
    }

    @Bean
    public GuideFluxAgent guideFluxAgent(StreamingChatModel streamingChatModel,
                                         SubMemoryIdSystemMessageProvider systemMessageProvider,
                                         ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(GuideFluxAgent.class)
                .streamingChatModel(streamingChatModel)
                .systemMessageProvider(systemMessageProvider)
                .chatMemoryProvider(chatMemoryProvider) // 使用默认记忆。因为subMemoryIdChatMemoryProvider记忆前期强制使用了JSON输出，会干扰当前智能体的输出
                .build();
    }

    @Bean
    public RagGuideFluxAgent ragGuideFluxAgent(StreamingChatModel streamingChatModel,
                                         SubMemoryIdSystemMessageProvider systemMessageProvider,
                                         ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(RagGuideFluxAgent.class)
                .streamingChatModel(streamingChatModel)
                .systemMessageProvider(systemMessageProvider)
                .chatMemoryProvider(chatMemoryProvider) // 使用默认记忆。因为subMemoryIdChatMemoryProvider记忆前期强制使用了JSON输出，会干扰当前智能体的输出
                .build();
    }

}
