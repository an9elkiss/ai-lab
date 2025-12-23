package com.weis.demo.agent;

import com.weis.demo.agent.typedkey.Image;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.K;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.V;

public interface DoormanAgent {

    @Agent(outputKey = "answer", description = "导购引导员")
    String answer(@V("consultation") String consultation, @K(Image.class) ImageContent image, @MemoryId String memoryId);
}