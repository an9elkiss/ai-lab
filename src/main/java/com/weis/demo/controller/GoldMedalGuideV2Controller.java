package com.weis.demo.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.weis.demo.agent.v2.*;
import com.weis.demo.dto.ItemDTO;
import com.weis.demo.dto.MemoryIdInfoDTO;
import com.weis.demo.dto.constant.AgentSystemMessage;
import com.weis.demo.dto.v2.GuideRespDTO;
import com.weis.demo.dto.v2.GuideRespExtDTO;
import com.weis.demo.dto.v2.IntentDTO;
import com.weis.demo.dto.v2.IntentRagDTO;
import com.weis.demo.memory.MemoryIdCreator;
import com.weis.demo.service.ItemService;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.model.input.PromptTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.weis.demo.dto.constant.AgentSystemMessage.*;
import static com.weis.demo.dto.constant.IntentTypeV2.*;
import static com.weis.demo.rag.v2.IntentContentInjector.INTENT_TEMPLATE;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@Slf4j
@RestController
@Tag(name = "金牌导购代理 V2", description = "智能服装导购代理API，支持图像识别和穿搭建议")
@RequestMapping("/agent/guide/v2")
public class GoldMedalGuideV2Controller {

    @Autowired
    private MemoryIdCreator memoryIdCreator;

    @Autowired
    private IntentAgent intentAgent;

    @Autowired
    private ImageIntentAgent imageIntentAgent;

    @Autowired
    private GuideAgent guideAgent;

    @Autowired
    private RAGGuideAgent ragGuideAgent;

    @Autowired
    private GuideFluxAgent guideFluxAgent;

    @Autowired
    private RagGuideFluxAgent ragGuideFluxAgent;

    @Autowired
    private ItemService itemService;

    public static final PromptTemplate ITEM_SEARCH_INTENT_TEMPLATE = PromptTemplate.from(
            """
                    {{userMessage}}
                    <image_content>
                    {{imageContent}}
                    </image_content>

                    <items>
                    {{items}}
                    </items>
                    """);

    @Operation(
            summary = "智能服装导购对话",
            description = "与金牌导购代理进行对话，获取专业的穿搭建议和商品推荐。支持多模态输入（文本+图像）和会话记忆功能。"
    )
    @PostMapping(value = "/consultation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,  produces = {"application/json"})
    public ResponseEntity<Object> consultation(
            @Parameter(description = "要分析的图片文件")
            @RequestParam(value = "image", required = false) MultipartFile imageFile,

            @Parameter(description = "咨询内容", example = "帮我找找有没有类似这种风格的裙子？下个月要去参加草坪婚礼，但希望平时在办公室穿也不夸张。")
            @RequestParam String consultation,

            @Parameter(description = "智能体记忆ID", example = "9a1b2c3d4e5f67890abcdef123456789")
            @RequestParam String memoryId
            ) throws IOException {

        createMemoryIdInfo(memoryId); //聊天上下文

        ImageContent imageContent = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            // 将上传的文件转换为 Base64 编码
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = imageFile.getContentType();
            imageContent = ImageContent.from(base64Image, mimeType);
        }

        IntentDTO intentDTO = null;
        if (imageContent != null) {
            intentDTO = imageIntentAgent.analyze(consultation, imageContent);
            // OpenAiTokenCountEstimator 不支持 ImageContent，所以无法添加记忆
//                    getSubMemoryId(memoryId, IMAGE_INTENT_AGENT_SYSTEM_MESSAGE));
        } else {
            intentDTO = intentAgent.analyze(consultation, getSubMemoryId(memoryId, INTENT_AGENT_SYSTEM_MESSAGE));
        }
        log.warn("IntentDTO: {}", JSONUtil.toJsonStr(intentDTO));
        String imageContentStr = intentDTO.getImageContent();
        imageContentStr = imageContentStr != null ? imageContentStr : "";

        if (GREETING.equals(intentDTO.getIntentType()) || OTHER.equals(intentDTO.getIntentType())) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userMessage", consultation);
            variables.put("imageContent", imageContentStr);
            String userInput = INTENT_TEMPLATE.apply(variables).text();
            log.warn("UserInput: {}", userInput);

            GuideRespDTO answer = guideAgent.answer(userInput, getSubMemoryId(memoryId, GUIDE_AGENT_SYSTEM_MESSAGE));
            return ResponseEntity.ok(answer);
        } if (DISCOVER_BRAND.equals(intentDTO.getIntentType())) {
            IntentRagDTO intentRagDTO = new IntentRagDTO();
            intentRagDTO.setEmbeddingQuery(intentDTO.getEmbeddingQuery());
            intentRagDTO.setUserInput(consultation);
            intentRagDTO.setImageContent(imageContentStr);

            GuideRespDTO answer = ragGuideAgent.answer(JSONUtil.toJsonStr(intentRagDTO), getSubMemoryId(memoryId, RAG_GUIDE_AGENT_SYSTEM_MESSAGE));
            return ResponseEntity.ok(answer);
        } if (ITEM_SEARCH.equals(intentDTO.getIntentType())) {
            String keyWords = intentDTO.getItemQueryKeyWords();
            keyWords = "红色"; // 测试代码
            List<ItemDTO> itemDTOS = itemService.search(keyWords);

            Map<String, Object> variables = new HashMap<>();
            variables.put("userMessage", consultation);
            variables.put("imageContent", imageContentStr);
            variables.put("items", itemDTOS != null && !itemDTOS.isEmpty() ? JSONUtil.toJsonStr(itemDTOS) : "");

            String userInput = ITEM_SEARCH_INTENT_TEMPLATE.apply(variables).text();
            log.warn("UserInput: {}", userInput);

            GuideRespDTO answer = guideAgent.answer(userInput, getSubMemoryId(memoryId, GUIDE_AGENT_SYSTEM_MESSAGE));

            GuideRespExtDTO answerExt = new GuideRespExtDTO();
            answerExt.setItems(itemDTOS);
            BeanUtil.copyProperties(answer, answerExt);

            return ResponseEntity.ok(answerExt);
        } else {
            throw new RuntimeException("Invalid intent type: " + intentDTO.getIntentType());
        }
    }

    private String getSubMemoryId(String memoryId, AgentSystemMessage agentSystemMessage) {
        return memoryId + "-" + agentSystemMessage.getCode();
    }

    private void createMemoryIdInfo(String memoryId) {
        MemoryIdInfoDTO memoryIdInfo = memoryIdCreator.parseMemoryId(memoryId);
        if (memoryIdInfo == null){
            memoryIdInfo = new MemoryIdInfoDTO();
            memoryIdInfo.setMemberId(1L);
            memoryIdInfo.setStoreId(1L);
            memoryIdInfo.setMemoryId(memoryId);
            memoryIdInfo.setShopId(1L);

            memoryIdCreator.createMemoryId(memoryIdInfo);
        }
        log.warn("MemoryIdInfo: {}", memoryIdInfo);
    }

    @Operation(
            summary = "流式输出演示",
            description = "流式输出"
    )
    @PostMapping(value = "/flux", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> flux(
            @Parameter(description = "要分析的图片文件")
            @RequestParam(value = "image", required = false) MultipartFile imageFile,

            @Parameter(description = "咨询内容", example = "你好")
            @RequestParam String consultation,

            @Parameter(description = "智能体记忆ID", example = "9a1b2c3d4e5f67890abcdef123456789")
            @RequestParam String memoryId
    ) throws IOException {

        createMemoryIdInfo(memoryId); //聊天上下文

        ImageContent imageContent = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            // 将上传的文件转换为 Base64 编码
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = imageFile.getContentType();
            imageContent = ImageContent.from(base64Image, mimeType);
        }

        IntentDTO intentDTO = null;
        if (imageContent != null) {
            intentDTO = imageIntentAgent.analyze(consultation, imageContent);
            // OpenAiTokenCountEstimator 不支持 ImageContent，所以无法添加记忆
//                    getSubMemoryId(memoryId, IMAGE_INTENT_AGENT_SYSTEM_MESSAGE));
        } else {
            intentDTO = intentAgent.analyze(consultation, getSubMemoryId(memoryId, INTENT_AGENT_SYSTEM_MESSAGE));
        }
        log.warn("IntentDTO: {}", JSONUtil.toJsonStr(intentDTO));
        String imageContentStr = intentDTO.getImageContent();
        imageContentStr = imageContentStr != null ? imageContentStr : "";

        if (GREETING.equals(intentDTO.getIntentType()) || OTHER.equals(intentDTO.getIntentType())) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userMessage", consultation);
            variables.put("imageContent", imageContentStr);
            String userInput = INTENT_TEMPLATE.apply(variables).text();
            log.warn("UserInput: {}", userInput);

            return guideFluxAgent.answer(userInput, getSubMemoryId(memoryId, GUIDE_FLUX_AGENT_SYSTEM_MESSAGE));
        } if (DISCOVER_BRAND.equals(intentDTO.getIntentType())) {
            IntentRagDTO intentRagDTO = new IntentRagDTO();
            intentRagDTO.setEmbeddingQuery(intentDTO.getEmbeddingQuery());
            intentRagDTO.setUserInput(consultation);
            intentRagDTO.setImageContent(imageContentStr);

            return ragGuideFluxAgent.answer(JSONUtil.toJsonStr(intentRagDTO), getSubMemoryId(memoryId, RAG_GUIDE_AGENT_SYSTEM_MESSAGE));
//        } if (ITEM_SEARCH.equals(intentDTO.getIntentType())) {
//            String keyWords = intentDTO.getItemQueryKeyWords();
//            keyWords = "红色"; // 测试代码
//            List<ItemDTO> itemDTOS = itemService.search(keyWords);
//
//            Map<String, Object> variables = new HashMap<>();
//            variables.put("userMessage", consultation);
//            variables.put("imageContent", imageContentStr);
//            variables.put("items", itemDTOS != null && !itemDTOS.isEmpty() ? JSONUtil.toJsonStr(itemDTOS) : "");
//
//            String userInput = ITEM_SEARCH_INTENT_TEMPLATE.apply(variables).text();
//            log.warn("UserInput: {}", userInput);
//
//            GuideRespDTO answer = guideAgent.answer(userInput, getSubMemoryId(memoryId, GUIDE_AGENT_SYSTEM_MESSAGE));
//
//            GuideRespExtDTO answerExt = new GuideRespExtDTO();
//            answerExt.setItems(itemDTOS);
//            BeanUtil.copyProperties(answer, answerExt);
//
//            return ResponseEntity.ok(answerExt);
        } else {
            throw new RuntimeException("Invalid intent type: " + intentDTO.getIntentType());
        }
    }

}
