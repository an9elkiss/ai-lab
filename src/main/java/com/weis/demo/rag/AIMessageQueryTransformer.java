package com.weis.demo.rag;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weis.demo.dto.AIMessageDTO;
import com.weis.demo.dto.DiscoverBrandKeyInfo;
import com.weis.demo.dto.constant.IntentType;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.weis.demo.dto.constant.IntentType.DISCOVER_BRAND;

@Slf4j
@Component
public class AIMessageQueryTransformer implements QueryTransformer {

    @Override
    public Collection<Query> transform(Query query) {
        try {
            String text = query.text();
            AIMessageDTO aiMessageDTO = JSONUtil.toBean(text, AIMessageDTO.class);
            
            // 获取意图类型
            IntentType intentType = IntentType.fromCode(aiMessageDTO.getIntentType());
            
            // 如果intentType = DISCOVER_BRAND
            if (intentType == DISCOVER_BRAND) {
                // keyInfo格式化成DiscoverBrandKeyInfo
                DiscoverBrandKeyInfo keyInfo = JSONUtil.toBean(
                    aiMessageDTO.getKeyInfo(), 
                    DiscoverBrandKeyInfo.class
                );
                
                // 把DiscoverBrandKeyInfo.embeddingQuery放入query.text()
                String embeddingQuery = keyInfo.getEmbeddingQuery();
                log.warn("DiscoverBrandKeyInfo.embeddingQuery: {}", embeddingQuery);
                
                // 返回Query
                Query transformedQuery = Query.from(embeddingQuery, query.metadata());
                return List.of(transformedQuery);
            }
            
            // 非DISCOVER_BRAND意图，返回原query
            return List.of(query);
            
        } catch (Exception e) {
            log.error("转换Query失败", e);
            // 异常情况返回原query
            return List.of(query);
        }
    }
}
