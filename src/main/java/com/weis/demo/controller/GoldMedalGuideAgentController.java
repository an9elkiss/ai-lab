package com.weis.demo.controller;

import com.weis.demo.agent.GoldMedalGuideAgent;
import com.weis.demo.dto.MemoryIdInfoDTO;
import com.weis.demo.memory.MemoryIdCreator;
import dev.langchain4j.data.message.ImageContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@RestController
@Tag(name = "金牌导购代理", description = "智能服装导购代理API，支持图像识别和穿搭建议")
@RequestMapping("/agent/guide")
@RequiredArgsConstructor
public class GoldMedalGuideAgentController {

    private final GoldMedalGuideAgent goldMedalGuideAgent;

    private final MemoryIdCreator memoryIdCreator;

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
        return result;
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
}
