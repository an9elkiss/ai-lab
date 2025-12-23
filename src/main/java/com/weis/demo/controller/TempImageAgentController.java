package com.weis.demo.controller;

import com.weis.demo.agent.TempImageAgent;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@RestController
@Tag(name = "临时图像代理", description = "基于千问VL-Max的临时图像分析代理API")
@RequestMapping("/agent/temp-image")
public class TempImageAgentController {

    private final TempImageAgent tempImageAgent;

    public TempImageAgentController(TempImageAgent tempImageAgent) {
        this.tempImageAgent = tempImageAgent;
    }

    @Operation(
            summary = "图像分析",
            description = "上传图片并提供文字描述，获取AI的图像分析结果"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取分析结果"),
            @ApiResponse(responseCode = "400", description = "请求参数无效"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String analyze(
            @Parameter(description = "用户消息内容", example = "请描述这张图片")
            @RequestParam String userMessage,

            @Parameter(description = "要分析的图片文件")
            @RequestParam("image") MultipartFile imageFile
    ) throws IOException {
        
        // 将上传的文件转换为 Base64 编码
        byte[] imageBytes = imageFile.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = imageFile.getContentType();
        
        // 创建 ImageContent
        ImageContent imageContent = ImageContent.from(base64Image, mimeType);
        
        // 调用代理分析
        Result<String> result = tempImageAgent.answer(userMessage, imageContent);
        return result.content();
    }

}
