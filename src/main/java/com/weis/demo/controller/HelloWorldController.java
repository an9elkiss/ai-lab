package com.weis.demo.controller;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * HelloWorld控制器
 * 演示基本的REST API接口
 *
 * @author weis
 * @date 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/hello")
@Tag(name = "HelloWorld接口", description = "演示接口")
public class HelloWorldController {

    @GetMapping("/world")
    @Operation(summary = "Hello World", description = "返回Hello World问候语")
    public Map<String, Object> helloWorld() {
        log.warn("调用HelloWorld接口");
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Hello World!");
        result.put("timestamp", new Date());
        result.put("status", "success");
        
        return result;
    }

    @GetMapping("/greet/{name}")
    @Operation(summary = "个性化问候", description = "根据传入的名字返回个性化问候语")
    public Map<String, Object> greet(
            @Parameter(description = "用户名称", required = true)
            @PathVariable String name) {
        log.warn("调用个性化问候接口，name: {}", name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Hello, " + name + "!");
        result.put("timestamp", new Date());
        result.put("status", "success");
        
        return result;
    }

    @PostMapping("/echo")
    @Operation(summary = "回显消息", description = "返回客户端发送的消息")
    public Map<String, Object> echo(@RequestBody Map<String, Object> payload) {
        log.warn("调用回显接口，payload: {}", JSONUtil.toJsonStr(payload));
        
        Map<String, Object> result = new HashMap<>();
        result.put("echo", payload);
        result.put("timestamp", new Date());
        result.put("status", "success");
        
        return result;
    }

    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "返回系统基本信息")
    public Map<String, Object> info() {
        log.warn("调用系统信息接口");
        
        Map<String, Object> result = new HashMap<>();
        result.put("application", "ai-lab");
        result.put("version", "1.0.0-SNAPSHOT");
        result.put("springBootVersion", "3.5.6");
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("timestamp", new Date());
        result.put("status", "success");
        
        return result;
    }

}

