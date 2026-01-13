package com.weis.demo.rag;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weis.demo.dto.AIMessageDTO;
import com.weis.demo.dto.constant.IntentType;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.QueryRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static com.weis.demo.dto.constant.IntentType.DISCOVER_BRAND;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIMessageQueryRouter implements QueryRouter {

    private final ContentRetriever contentRetriever;

    @Override
    public Collection<ContentRetriever> route(Query query) {

        try {
            ChatMessage chatMessage = query.metadata().chatMessage();
            UserMessage userMessage = (UserMessage) chatMessage;
            String text = userMessage.singleText();
            AIMessageDTO aiMessageDTO = JSONUtil.toBean(text, AIMessageDTO.class);

            // 获取意图类型
            IntentType intentType = IntentType.fromCode(aiMessageDTO.getIntentType());

            // 如果intentType = DISCOVER_BRAND
            if (intentType == DISCOVER_BRAND) {
                return singletonList(contentRetriever);
            }

            // 非DISCOVER_BRAND意图，跳过RAG
            return emptyList();

        } catch (Exception e) {
            log.error("路由Query失败", e);
            // 异常情况返回原query
            return emptyList();
        }
    }
}
