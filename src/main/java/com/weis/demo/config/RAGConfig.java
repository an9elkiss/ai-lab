package com.weis.demo.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@Configuration
public class RAGConfig {


    @Bean
    TokenCountEstimator tokenCountEstimator() {
        // DeepSeek 使用类似 GPT 的分词器，使用 GPT-3.5-turbo 的分词器作为兼容选项
        return new OpenAiTokenCountEstimator("gpt-3.5-turbo");
    }

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
    EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel,
                                               ResourceLoader resourceLoader,
                                               TokenCountEstimator tokenizer,
                                               RestClient restClient) throws IOException {

        // 1. 创建 Elasticsearch 8 嵌入存储
        EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                .restClient(restClient)
                .build();

        // 2. 加载示例文档
        Resource resource = resourceLoader.getResource("classpath:ski-equipment-knowledge.txt");
        Document document = loadDocument(resource.getFile().toPath(), new TextDocumentParser());

        // 3. 分割文档并存储到 Elasticsearch
        // 将文档分割成每段 300 个 token
        // 将段落转换为嵌入向量
        // 将嵌入向量存储到 Elasticsearch
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(300, 0, tokenizer);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

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


}
