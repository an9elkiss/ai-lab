package com.weis.demo.config;

import com.weis.demo.agent.DoormanAgent;
import com.weis.demo.agent.GoldMedalGuideAgent;
import com.weis.demo.agent.VisualAnalyzerAgent;
import com.weis.demo.agent.VisualGuideAgent;
import com.weis.demo.agent.listener.ChatModelListenerImpl;
import com.weis.demo.agent.provider.DemoSystemMessageProvider;
import com.weis.demo.agent.typedkey.Image;
import com.weis.demo.tool.ItemTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.http.client.HttpClientBuilderFactory;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@Configuration
public class GoldMedalGuideAgentConfig {

    @Bean
    ChatModelListener chatModelListener() {
        // 注册自定义的聊天模型监听器，用于监控和记录所有聊天模型交互
        return new ChatModelListenerImpl();
    }

    @Bean
    public QwenChatModel qwenChatModel(
            @Value("${langchain4j.dashscope.chat-model.api-key}") String apiKey,
            @Value("${langchain4j.dashscope.chat-model.model-name}") String modelName,
            @Value("${langchain4j.dashscope.chat-model.temperature}") Float temperature,
            ChatModelListener chatModelListener) {
        
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .listeners(List.of(chatModelListener))
                .build();
    }

    @Bean
    public HttpClientBuilderFactory httpClientBuilderFactory(){
        return new SpringRestClientBuilderFactory();
    }

    @Bean
    public ChatModel chatModel(
            @Value("${langchain4j.open-ai.chat-model.api-key}") String apiKey,
            @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName,
            @Value("${langchain4j.open-ai.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.open-ai.chat-model.temperature}") Double temperature,
            HttpClientBuilderFactory httpClientBuilderFactory,
            ChatModelListener chatModelListener) {


        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(temperature)
                .listeners(List.of(chatModelListener))
                .httpClientBuilder(httpClientBuilderFactory.create())
                .build();
    }

    @Bean
    TokenCountEstimator tokenCountEstimator() {
        // DeepSeek 使用类似 GPT 的分词器，使用 GPT-3.5-turbo 的分词器作为兼容选项
        return new OpenAiTokenCountEstimator("gpt-3.5-turbo");
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider(TokenCountEstimator tokenizer) {
        return memoryId -> TokenWindowChatMemory.builder()
                .id(memoryId)
                .maxTokens(5000, tokenizer)
                .build();
    }

    @Bean
    EmbeddingModel embeddingModel() {
        // Not the best embedding model, but good enough for this demo
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel, ResourceLoader resourceLoader, TokenCountEstimator tokenizer) throws IOException {

        // Normally, you would already have your embedding store filled with your data.
        // However, for the purpose of this demonstration, we will:

        // 1. Create an in-memory embedding store
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 2. Load an example document ("Miles of Smiles" terms of use)
        Resource resource = resourceLoader.getResource("classpath:ski-equipment-knowledge.txt");
        Document document = loadDocument(resource.getFile().toPath(), new TextDocumentParser());

        // 3. Split the document into segments 100 tokens each
        // 4. Convert segments into embeddings
        // 5. Store embeddings into embedding store
        // All this can be done manually, but we will use EmbeddingStoreIngestor to automate this:
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

//    @Bean
    public ToolProvider toolProvider(){
        McpTransport transport = new StreamableHttpMcpTransport.Builder()
                .url("http://localhost:9000/sse")
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();
        return toolProvider;
    }

    @Bean
    public VisualAnalyzerAgent visualAnalyzerAgent(QwenChatModel chatModel, ChatMemoryProvider chatMemoryProvider) {
        return AgenticServices.agentBuilder(VisualAnalyzerAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
//                .summarizedContext("goldMedalGuideAgent")
                .build();
    }

    @Bean
    public GoldMedalGuideAgent goldMedalGuideAgent(ChatModel chatModel,
                                                   ChatMemoryProvider chatMemoryProvider,
                                                   DemoSystemMessageProvider systemMessageProvider,
                                                   ContentRetriever contentRetriever,
                                                   ItemTools itemTools
//                                                   ToolProvider toolProvider
    ) {
        return AgenticServices.agentBuilder(GoldMedalGuideAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(systemMessageProvider)
//                .toolProvider(toolProvider)
//                .contentRetriever(contentRetriever)
//                .tools(itemTools)
                .build();
    }

    @Bean
    public VisualGuideAgent visualGuideAgent(
            VisualAnalyzerAgent visualAnalyzerAgent,
            GoldMedalGuideAgent goldMedalGuideAgent) {
        
        return AgenticServices.sequenceBuilder(VisualGuideAgent.class)
                .subAgents(visualAnalyzerAgent, goldMedalGuideAgent)
                .build();
    }

    @Bean
    public DoormanAgent doormanAgent(
            VisualGuideAgent visualGuideAgent,
            GoldMedalGuideAgent goldMedalGuideAgent) {
        
        return AgenticServices.conditionalBuilder(DoormanAgent.class)
                .subAgents(agenticScope-> agenticScope.readState(Image.class) != null, visualGuideAgent)
                .subAgents(agenticScope-> agenticScope.readState(Image.class) == null, goldMedalGuideAgent)
                .outputKey("answer")
                .build();
    }

}
