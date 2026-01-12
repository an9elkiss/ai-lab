package com.weis.demo.controller;

import com.weis.demo.agent.GoldMedalGuideAgent;
import dev.langchain4j.data.message.ImageContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@RestController
@Tag(name = "金牌导购代理", description = "智能服装导购代理API，支持图像识别和穿搭建议")
@RequestMapping("/agent/guide")
public class GoldMedalGuideAgentController {

    private final GoldMedalGuideAgent goldMedalGuideAgent;

    public GoldMedalGuideAgentController(GoldMedalGuideAgent goldMedalGuideAgent) {
        this.goldMedalGuideAgent = goldMedalGuideAgent;
    }

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

            @Parameter(description = "会员ID", example = "123")
            @RequestParam Long memberId
            ) throws IOException {

        ImageContent imageContent = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            // 将上传的文件转换为 Base64 编码
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = imageFile.getContentType();
            imageContent = ImageContent.from(base64Image, mimeType);
        }

        String result = goldMedalGuideAgent.answer(consultation, memberId.toString());
        return result;
    }
}
