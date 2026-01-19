package com.weis.demo.dto.command;

import lombok.Data;

import java.util.List;

/**
 * AI智能体测试用例查询命令对象
 *
 * @author weis
 * @date 2025-01-19
 */
@Data
public class AiAgentTestCaseFindCmd {

    /**
     * 测试用例名称（模糊查询）
     */
    private String caseName;

    private List<String> caseNames;

    /**
     * 用例类型：function/ performance/ edge/ scenario
     */
    private String caseType;

    /**
     * 优先级：1-紧急 2-高 3-中 4-低
     */
    private Integer priority;

    /**
     * 状态：draft/ active/ inactive/ failed
     */
    private Integer status;
}
