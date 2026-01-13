package com.weis.demo.config;

import com.weis.demo.rag.AIMessageContentInjector;
import com.weis.demo.rag.AIMessageQueryRouter;
import com.weis.demo.rag.AIMessageQueryTransformer;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RAGConfig {

    @Bean
    EmbeddingModel embeddingModel() {
        // 使用阿里千问的 Embedding 模型
        return QwenEmbeddingModel.builder()
                .apiKey(System.getenv("QWEN_API_KEY"))
                .modelName("text-embedding-v3")
                .dimension(1024)
                .build();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(RestClient restClient) {

        // 1. 创建 Elasticsearch 8 嵌入存储
        EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                .restClient(restClient)
                .indexName("bosideng-brand-story")
                .build();
        return embeddingStore;
    }

    @Bean
    ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {

        // You will need to adjust these parameters to find the optimal setting,
        // which will depend on multiple factors, for example:
        // - The nature of your data
        // - The embedding model you are using
        int maxResults = 1;
        double minScore = 0.6;

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }

    @Bean
    RetrievalAugmentor retrievalAugmentor(AIMessageQueryTransformer queryTransformer, AIMessageQueryRouter queryRouter) {

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .queryRouter(queryRouter)
                .contentInjector(new AIMessageContentInjector(PromptTemplate.from("""
                    {{userMessage}}

                    **注意**回答时基于以下事实:
                    {{contents}}""")))
                .build();

        return retrievalAugmentor;
    }


}
