package com.weis.demo.service.test;

import cn.hutool.json.JSONUtil;
import com.weis.demo.dto.AIMessageDTO;
import com.weis.demo.dto.AiAgentTestResultDTO;
import com.weis.demo.entity.AiAgentTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Service("goldMedalGuideAgentControllerTestService")
public class GoldMedalGuideAgentControllerTestService implements AgentTestHandler {

    @Autowired
    private RestTemplate restTemplate;

    public String consultation(String consultation, String memoryId) {
        log.warn("通过RestTemplate调用consultation接口: consultation={}, memoryId={}",
                consultation, memoryId);

        // 构建请求URL
        String url = "http://localhost:9000/agent/guide/consultation";

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 构建请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("consultation", consultation);
        body.add("memoryId", memoryId);

        // 如果有图片文件，添加到请求体
//        if (imageFile != null && !imageFile.isEmpty()) {
//            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
//                @Override
//                public String getFilename() {
//                    return imageFile.getOriginalFilename();
//                }
//            };
//            body.add("image", resource);
//        }

        // 构建请求实体
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 发送POST请求
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        String result = response.getBody();
        log.warn("RestTemplate调用成功，响应状态: {}, 响应内容: {}",
                response.getStatusCode(), result);

        return result;
    }

    @Override
    public void handle(AiAgentTestResultDTO testResult) {
        AiAgentTestCase testCase = testResult.getAiAgentTestCase();
        String userInputText = testCase.getUserInputText();
        String memoryId = UUID.randomUUID().toString().replace("-", "");
        
        log.info("生成的memoryId: {}", memoryId);
        log.info("处理测试用例，用户输入: {}", userInputText);
        
        // 调用咨询方法进行测试
        String result = consultation(userInputText, memoryId);
        
        log.info("测试结果: {}", result);

        testResult.setActualOutput(result);

        AIMessageDTO aiMessageDTO = JSONUtil.toBean(result, AIMessageDTO.class);
        String actualIntentType = aiMessageDTO.getIntentType();
        String actualSubsequentFlow = aiMessageDTO.getSubsequentFlow();

        // 与测试用例中的预期值进行断言
        String expectedIntentType = testCase.getExpectedIntentType();
        String expectedSubsequentFlow = testCase.getExpectedSubsequentFlow();

        // 断言意图类型
        if (expectedIntentType != null && !expectedIntentType.equals(actualIntentType)) {
            String errorMsg = String.format("意图类型断言失败: 期望值=%s, 实际值=%s", expectedIntentType, actualIntentType);
            log.error(errorMsg);
            throw new AssertionError(errorMsg);
        }

        // 断言后续流程
        if (expectedSubsequentFlow != null && !expectedSubsequentFlow.equals(actualSubsequentFlow)) {
            String errorMsg = String.format("后续流程断言失败: 期望值=%s, 实际值=%s", expectedSubsequentFlow, actualSubsequentFlow);
            log.error(errorMsg);
            throw new AssertionError(errorMsg);
        }

        log.warn("断言通过: intentType={}, subsequentFlow={}", actualIntentType, actualSubsequentFlow);
    }
}
