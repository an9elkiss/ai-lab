package com.weis.demo.entity;

import lombok.Data;

import java.util.Date;

/**
 * 智能体测试用例实体类
 *
 * @author weis
 * @date 2025-01-19
 */
@Data
public class AiAgentTestCase {

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 测试用例名称
     */
    private String caseName;

    /**
     * 用例描述
     */
    private String description;

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

    /**
     * 用户输入文本
     */
    private String userInputText;

    /**
     * 用户输入图片URL
     */
    private String userInputImageUrl;

    /**
     * 会话上下文（历史对话记录，JSON数组格式）
     */
    private String sessionContext;

    /**
     * 测试用例处理程序
     */
    private String handler;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后修改时间
     */
    private Date lastModifyTime;
}
