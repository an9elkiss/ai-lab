package com.weis.demo.controller;

import cn.hutool.json.JSONUtil;
import com.weis.demo.agent.GoldMedalGuideAgent;
import com.weis.demo.dto.AIMessageDTO;
import com.weis.demo.dto.MemoryIdInfoDTO;
import com.weis.demo.dto.constant.IntentType;
import com.weis.demo.memory.MemoryIdCreator;
import com.weis.demo.rag.RegDocumentSplitter;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.weis.demo.dto.constant.IntentType.DISCOVER_BRAND;

@Slf4j
@RestController
@Tag(name = "金牌导购代理", description = "智能服装导购代理API，支持图像识别和穿搭建议")
@RequestMapping("/agent/guide")
@RequiredArgsConstructor
public class GoldMedalGuideAgentController {

    private final GoldMedalGuideAgent goldMedalGuideAgent;

    private final MemoryIdCreator memoryIdCreator;

    private final RestClient restClient;
    
    private final EmbeddingModel embeddingModel;

    @Operation(
            summary = "智能服装导购对话",
            description = "与金牌导购代理进行对话，获取专业的穿搭建议和商品推荐。支持多模态输入（文本+图像）和会话记忆功能。"
    )
    @PostMapping(value = "/consultation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String consultation(
            @Parameter(description = "要分析的图片文件")
            @RequestParam(value = "image", required = false) MultipartFile imageFile,

            @Parameter(description = "咨询内容", example = "帮我找找有没有类似这种风格的裙子？下个月要去参加草坪婚礼，但希望平时在办公室穿也不夸张。")
            @RequestParam String consultation,

            @Parameter(description = "智能体记忆ID", example = "aaa")
            @RequestParam String memoryId
            ) throws IOException {

        ImageContent imageContent = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            // 将上传的文件转换为 Base64 编码
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = imageFile.getContentType();
            imageContent = ImageContent.from(base64Image, mimeType);
        }

        String result = goldMedalGuideAgent.answer(consultation, memoryId);

        // 需要再次执行，如：使用RAG
        if (oneMoreTime(result)) {
            result = goldMedalGuideAgent.answer(result, memoryId);
        }

        return result;
    }

    private boolean oneMoreTime(String result){

        if (result == null || !result.startsWith("{")) return false;

        AIMessageDTO aiMessageDTO = JSONUtil.toBean(result, AIMessageDTO.class);

        // 获取意图类型
        IntentType intentType = IntentType.fromCode(aiMessageDTO.getIntentType());

        // 如果intentType = DISCOVER_BRAND
        return intentType == DISCOVER_BRAND;
    }

    @Operation(
            summary = "创建MemoryId",
            description = "根据提供的会话信息创建一个新的MemoryId，并将信息存储到Redis中。"
    )
    @PostMapping("/memory")
    public ResponseEntity<String> createMemoryId(
            @Parameter(description = "MemoryId信息", required = true)
            @RequestBody MemoryIdInfoDTO memoryIdInfo) {
        
        try {
            String memoryId = memoryIdCreator.createMemoryId(memoryIdInfo);
            return ResponseEntity.ok(memoryId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "解析MemoryId",
            description = "根据MemoryId从Redis中获取对应的会话信息，包括商店ID、会员ID、代理名称等。"
    )
    @GetMapping("/memory/{memoryId}")
    public ResponseEntity<MemoryIdInfoDTO> parseMemoryId(
            @Parameter(description = "要解析的MemoryId", example = "550e8400e29b41d4a716446655440000")
            @PathVariable String memoryId) {
        
        MemoryIdInfoDTO memoryIdInfo = memoryIdCreator.parseMemoryId(memoryId);
        
        if (memoryIdInfo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(memoryIdInfo);
    }

    @Operation(
            summary = "上传文本文件进行知识库嵌入",
            description = "上传一个文本文件，将其内容分割并嵌入到指定的Elasticsearch索引中，用于后续的RAG检索。"
    )
    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> ingestTextFile(
            @Parameter(description = "要嵌入的文本文件", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Elasticsearch索引名称", required = false, example = "huawei-phone-story")
            @RequestParam(value = "indexName", required = false, defaultValue = "huawei-phone-story") String indexName) {
        
        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }
            
            // 验证文件类型是否为文本文件
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("text/")) {
                return ResponseEntity.badRequest().body("只支持文本文件格式");
            }
            
            // 验证索引名称
            if (indexName == null || indexName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("索引名称不能为空");
            }
            
            // 读取文件内容
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Document document = Document.from(content);
            
            // 根据指定的索引名称创建 EmbeddingStore
            EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                    .restClient(restClient)
                    .indexName(indexName)
                    .build();
            
            // 创建文档分割器
            DocumentSplitter documentSplitter = new RegDocumentSplitter("=Slice Start=(.*?)=Slice End=");
            
            // 创建 EmbeddingStoreIngestor
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(documentSplitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            
            // 调用嵌入存储器进行数据嵌入
            ingestor.ingest(document);
            
            return ResponseEntity.ok("文件 '" + file.getOriginalFilename() + 
                    "' 已成功嵌入到索引 '" + indexName + "' 中");
            
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("读取文件时发生错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("嵌入过程中发生错误", e);
            return ResponseEntity.internalServerError()
                    .body("嵌入过程中发生错误: " + e.getMessage());
        }
    }
}
