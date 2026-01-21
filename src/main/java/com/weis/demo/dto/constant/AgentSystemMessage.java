package com.weis.demo.dto.constant;

import com.weis.demo.agent.v2.GuideAgent;
import com.weis.demo.agent.v2.ImageIntentAgent;
import com.weis.demo.agent.v2.IntentAgent;
import com.weis.demo.agent.v2.RAGGuideAgent;

public enum AgentSystemMessage {

    IMAGE_INTENT_AGENT_SYSTEM_MESSAGE("100001", ImageIntentAgent.SYSTEM_MESSAGE),
    INTENT_AGENT_SYSTEM_MESSAGE("100002", IntentAgent.SYSTEM_MESSAGE),
    GUIDE_AGENT_SYSTEM_MESSAGE("100003", GuideAgent.SYSTEM_MESSAGE),
    RAG_GUIDE_AGENT_SYSTEM_MESSAGE("100004", RAGGuideAgent.SYSTEM_MESSAGE),


    ;

    private String code;

    private String message;

    AgentSystemMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static AgentSystemMessage getByCode(String code) {
        for (AgentSystemMessage agentSystemMessage : AgentSystemMessage.values()) {
            if (agentSystemMessage.getCode().equals(code)) {
                return agentSystemMessage;
            }
        }
        return null;
    }
}
