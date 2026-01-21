package com.weis.demo.agent.provider;

import com.weis.demo.dto.constant.AgentSystemMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * 子记忆ID系统消息提供者
 * 根据memoryId解析出对应的系统消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubMemoryIdSystemMessageProvider implements Function<Object, String> {

    /**
     * 根据memoryId获取对应的系统消息
     * 
     * @param memoryId 记忆ID，格式为：xxx-xxx-code
     * @return 对应code的系统消息内容
     */
    @Override
    public String apply(Object memoryId) {
        String[] parts = memoryId.toString().split("-");
        String code = parts[parts.length - 1];

        return AgentSystemMessage.getByCode(code).getMessage();
    }
    

}
