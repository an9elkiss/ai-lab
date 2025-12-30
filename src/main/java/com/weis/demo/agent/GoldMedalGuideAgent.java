package com.weis.demo.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.*;

public interface GoldMedalGuideAgent {

    @Agent(name = "goldMedalGuideAgent", outputKey = "answer", description = "金牌服装导购")
    String answer(@UserMessage @V("consultation") String consultation, @MemoryId String memoryId);
}