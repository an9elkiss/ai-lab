package com.weis.demo.config;

import com.weis.demo.agent.DoormanAgent;
import com.weis.demo.agent.GoldMedalGuideAgent;
import com.weis.demo.agent.VisualAnalyzerAgent;
import com.weis.demo.agent.VisualGuideAgent;
import com.weis.demo.agent.provider.GoldMedalGuideSystemMessageProvider;
import com.weis.demo.agent.typedkey.Image;
import com.weis.demo.tool.ItemTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {


    @Bean
    public VisualAnalyzerAgent visualAnalyzerAgent(QwenChatModel chatModel, ChatMemoryProvider chatMemoryProvider) {
        return AgenticServices.agentBuilder(VisualAnalyzerAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
//                .summarizedContext("goldMedalGuideAgent")
                .build();
    }

    @Bean
    public GoldMedalGuideAgent goldMedalGuideAgent(ChatModel chatModel,
                                                   ChatMemoryProvider chatMemoryProvider,
                                                   GoldMedalGuideSystemMessageProvider systemMessageProvider,
                                                   RetrievalAugmentor retrievalAugmentor,
                                                   ItemTools itemTools
//                                                   ToolProvider toolProvider
    ) {
        return AgenticServices.agentBuilder(GoldMedalGuideAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(systemMessageProvider)
                .retrievalAugmentor(retrievalAugmentor)
//                .toolProvider(toolProvider)
                .tools(itemTools)
                .build();
    }

    @Bean
    public VisualGuideAgent visualGuideAgent(
            VisualAnalyzerAgent visualAnalyzerAgent,
            GoldMedalGuideAgent goldMedalGuideAgent) {

        return AgenticServices.sequenceBuilder(VisualGuideAgent.class)
                .subAgents(visualAnalyzerAgent, goldMedalGuideAgent)
                .build();
    }

    @Bean
    public DoormanAgent doormanAgent(
            VisualGuideAgent visualGuideAgent,
            GoldMedalGuideAgent goldMedalGuideAgent) {

        return AgenticServices.conditionalBuilder(DoormanAgent.class)
                .subAgents(agenticScope-> agenticScope.readState(Image.class) != null, visualGuideAgent)
                .subAgents(agenticScope-> agenticScope.readState(Image.class) == null, goldMedalGuideAgent)
                .outputKey("answer")
                .build();
    }
}
