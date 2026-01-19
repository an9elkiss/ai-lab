package com.weis.demo.controller;

import com.weis.demo.dto.command.AiAgentTestCaseFindCmd;
import com.weis.demo.entity.AiAgentTestCase;
import com.weis.demo.service.AgentTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "AI智能体测试", description = "AI智能体测试用例管理API")
@RequestMapping("/agent/test")
@RequiredArgsConstructor
public class AgentTestController {

    private final AgentTestService agentTestService;

    @Operation(
            summary = "测试指定用例",
            description = "根据条件测试AI智能体测试用例，支持按用例名称、类型、优先级、状态等条件筛选"
    )
    @PostMapping("/do")
    public ResponseEntity<Void> test(
            @Parameter(description = "用例查询条件", required = true)
            @RequestBody AiAgentTestCaseFindCmd cmd) {

        log.warn("查询测试用例，条件: {}", cn.hutool.json.JSONUtil.toJsonStr(cmd));
        agentTestService.test(cmd);
        return ResponseEntity.ok(null);
    }

}

