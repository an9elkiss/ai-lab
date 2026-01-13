package com.weis.demo.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 初始化时指定一个正则表达式，用于将文档分割成多个段落。
 */
public class RegDocumentSplitter implements DocumentSplitter {

    private final Pattern pattern;

    /**
     * 构造函数，指定用于分割文档的正则表达式
     * @param regex 正则表达式字符串
     */
    public RegDocumentSplitter(String regex) {
        // 使用 DOTALL 模式，使 . 可以匹配换行符
        this.pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    @Override
    public List<TextSegment> split(Document document) {
        if (document == null || document.text() == null || document.text().isEmpty()) {
            return List.of();
        }

        String text = document.text();
        Matcher matcher = pattern.matcher(text);
        
        List<TextSegment> textSegments = new ArrayList<>();
        
        // 查找所有匹配的部分
        while (matcher.find()) {
            // 获取第一个捕获组（括号内的内容）
            String captured = matcher.group(1);
            if (captured != null) {
                String trimmed = captured.trim();
                if (!trimmed.isEmpty()) {
                    textSegments.add(TextSegment.from(trimmed, document.metadata()));
                }
            }
        }
        
        return textSegments;
    }
}
