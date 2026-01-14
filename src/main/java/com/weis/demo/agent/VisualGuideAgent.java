package com.weis.demo.agent;

import com.weis.demo.agent.typedkey.Image;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.K;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface VisualGuideAgent {

    @Agent(outputKey = "answer", description = "多模态金牌服装导购")
    String analyze(@UserMessage @V("consultation") String consultation, @UserMessage @K(Image.class) ImageContent image, @MemoryId String memoryId);
}