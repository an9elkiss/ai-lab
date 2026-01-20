package com.weis.demo.config;

import com.weis.demo.rag.v2.IntentContentInjector;
import com.weis.demo.rag.v2.IntentQueryTransformer;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RAGV2Config {


    @Bean
    RetrievalAugmentor retrievalAugmentorV2(IntentQueryTransformer queryTransformer, ContentRetriever contentRetriever) {

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .queryRouter(new DefaultQueryRouter(contentRetriever))
                .contentInjector(new IntentContentInjector(PromptTemplate.from("""
                    {{userMessage}}

                    <rag_result>
                    {{contents}}
                    </rag_result>
                    """)))
                .build();

        return retrievalAugmentor;
    }


}
