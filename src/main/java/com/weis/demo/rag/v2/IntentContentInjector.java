package com.weis.demo.rag.v2;

import cn.hutool.json.JSONUtil;
import com.weis.demo.dto.v2.IntentRagDTO;
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
public class IntentContentInjector extends DefaultContentInjector {

    public IntentContentInjector(PromptTemplate promptTemplate) {
        super(promptTemplate);
    }

    @Override
    public ChatMessage inject(List<Content> contents, ChatMessage chatMessage) {

        UserMessage userMessage = (UserMessage) chatMessage;
        String singleText = userMessage.singleText();
        IntentRagDTO aiMessageDTO = JSONUtil.toBean(singleText, IntentRagDTO.class);

        chatMessage = UserMessage.from(aiMessageDTO.getUserInput());

        if (contents.isEmpty()) {
            Content content = Content.from("知识库中不包含回答问题所需的信息，**不得编造信息**。");
            contents.add(content);
        }

        Prompt prompt = createPrompt(chatMessage, contents);
        return userMessage.toBuilder()
                .contents(List.of(TextContent.from(prompt.text())))
                .build();
    }

}
