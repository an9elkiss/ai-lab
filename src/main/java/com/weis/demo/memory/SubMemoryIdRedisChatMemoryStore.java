package com.weis.demo.memory;

import com.weis.demo.agent.provider.SubMemoryIdSystemMessageProvider;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubMemoryIdRedisChatMemoryStore extends RedisChatMemoryStore {

    @Autowired
    private SubMemoryIdSystemMessageProvider subMemoryIdSystemMessageProvider;

    /**
     * 获取聊天消息列表，并替换SystemMessage为根据memoryId动态生成的系统消息
     * 
     * @param memoryId 内存标识，格式为"baseId-code"
     * @return 聊天消息列表，其中第一条消息为动态生成的SystemMessage
     * @throws IllegalArgumentException 如果memoryId格式错误
     * @throws IllegalStateException 如果消息列表第一条不是SystemMessage
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String baseId = getBaseMemoryId(memoryId);

        List<ChatMessage> messages = super.getMessages(baseId);
        if (messages == null || messages.isEmpty()) {return messages;}

        String systemMessage = subMemoryIdSystemMessageProvider.apply(memoryId);

        // 断言messages[0]是SystemMessage
        if (!(messages.getFirst() instanceof SystemMessage)) {
            log.error("messages[0]不是SystemMessage, memoryId: {}, messageType: {}", 
                    memoryId, messages.getFirst().getClass().getSimpleName());
            throw new IllegalStateException("messages[0]必须是SystemMessage");
        }

        // 将messages[0]替换为新的systemMessage
        List<ChatMessage> result = new ArrayList<>(messages);
        result.set(0, SystemMessage.from(systemMessage));

        return result;
    }

    private String getBaseMemoryId(Object memoryId) {
        String[] parts = memoryId.toString().split("-");
        if (parts.length != 2) {
            log.error("memoryId格式错误, memoryId: {}", memoryId);
            throw new IllegalArgumentException("memoryId格式错误，应为: baseId-code");
        }
        String baseId = parts[0];
        return baseId;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String baseId = getBaseMemoryId(memoryId);
        super.updateMessages(baseId, messages);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String baseId = getBaseMemoryId(memoryId);
        super.deleteMessages(baseId);
    }
}
