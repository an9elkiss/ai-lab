package com.weis.demo.rag;

import cn.hutool.json.JSONUtil;
import com.weis.demo.dto.AIMessageDTO;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AIMessageContentInjector extends DefaultContentInjector {

    public AIMessageContentInjector(PromptTemplate promptTemplate) {
        super(promptTemplate);
    }

    @Override
    public ChatMessage inject(List<Content> contents, ChatMessage chatMessage) {

        // 如果用户输入的是AIMessageDTO，将原始的用户输入作为UserMessage
        try {
            UserMessage userMessage = (UserMessage) chatMessage;
            String singleText = userMessage.singleText();

            AIMessageDTO aiMessageDTO = JSONUtil.toBean(singleText, AIMessageDTO.class);

            chatMessage = UserMessage.from(aiMessageDTO.getUserInput());
        } catch (Exception e) {
            // 否则，不进行inject
            log.error("AIMessageContentInjector.inject error", e);
            return chatMessage;
        }

        if (contents.isEmpty()) {
            Content content = Content.from("知识库中不包含回答问题所需的信息，**不得编造信息**。");
            contents.add(content);
        }

        Prompt prompt = createPrompt(chatMessage, contents);
        if (chatMessage instanceof UserMessage userMessage) {
            return userMessage.toBuilder()
                    .contents(List.of(TextContent.from(prompt.text())))
                    .build();
        } else {
            return prompt.toUserMessage();
        }
    }

}
