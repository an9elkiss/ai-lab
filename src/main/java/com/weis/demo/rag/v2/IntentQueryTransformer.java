package com.weis.demo.rag.v2;

import cn.hutool.json.JSONUtil;
import com.weis.demo.dto.v2.IntentRagDTO;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class IntentQueryTransformer implements QueryTransformer {

    @Override
    public Collection<Query> transform(Query query) {
        String text = query.text();
        IntentRagDTO intentDTO = JSONUtil.toBean(text, IntentRagDTO.class);
        String embeddingQuery = intentDTO.getEmbeddingQuery();
        log.warn("DiscoverBrandKeyInfo.embeddingQuery: {}", embeddingQuery);

        // 返回Query
        Query transformedQuery = Query.from(embeddingQuery, query.metadata());
        return List.of(transformedQuery);

    }
}
