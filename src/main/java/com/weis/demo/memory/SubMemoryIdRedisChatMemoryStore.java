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

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String[] parts = memoryId.toString().split("-");
        if (parts.length != 2) {
            log.error("memoryId格式错误, memoryId: {}", memoryId);
            throw new IllegalArgumentException("memoryId格式错误，应为: baseId-code");
        }
        String baseId = parts[0];

        List<ChatMessage> messages = super.getMessages(baseId);
        if (messages == null || messages.isEmpty()) {return messages;}

        String systemMessage = subMemoryIdSystemMessageProvider.apply(memoryId);

        // 断言messages[0]是SystemMessage
        if (!(messages.get(0) instanceof SystemMessage)) {
            log.error("messages[0]不是SystemMessage, memoryId: {}, messageType: {}", 
                    memoryId, messages.get(0).getClass().getSimpleName());
            throw new IllegalStateException("messages[0]必须是SystemMessage");
        }

        // 将messages[0]替换为新的systemMessage
        List<ChatMessage> result = new ArrayList<>(messages);
        result.set(0, SystemMessage.from(systemMessage));

        return result;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        super.updateMessages(memoryId, messages);
    }
}
