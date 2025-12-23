package com.weis.demo.agent.listener;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 客户支持聊天模型监听器
 * 用于监听和记录所有与聊天模型的交互，包括请求、响应和错误
 */
@Component
public class ChatModelListenerImpl implements ChatModelListener {

    private static final Logger log = LoggerFactory.getLogger(ChatModelListenerImpl.class);

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        log.info("onRequest(): {}", requestContext.chatRequest());
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        log.info("onResponse(): {}", responseContext.chatResponse());
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        log.info("onError(): {}", errorContext.error().getMessage());
    }
}
