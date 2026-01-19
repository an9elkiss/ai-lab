package com.weis.demo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 智能体测试结果实体类
 *
 * @author weis
 * @date 2025-01-19
 */
@Data
public class AiAgentTestResult {

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 测试用例ID
     */
    private Long caseId;

    /**
     * 实际输出（完整的JSON响应）
     */
    private String actualOutput;

    /**
     * 执行时间
     */
    private Date executionTime;

    /**
     * 执行耗时
     */
    private Integer executionDuration;

    /**
     * 执行状态：success/ failed/ timeout
     */
    private String executionStatus;

    /**
     * 结果匹配度评分（0-100）
     */
    private BigDecimal resultMatchScore;

    /**
     * 预期与实际差异详情
     */
    private String differences;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 智能体版本号
     */
    private String agentVersion;

    /**
     * 模型版本号
     */
    private String modelVersion;

    /**
     * 测试环境：dev/ staging/ prod
     */
    private String testEnvironment;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后修改时间
     */
    private Date lastModifyTime;
}
