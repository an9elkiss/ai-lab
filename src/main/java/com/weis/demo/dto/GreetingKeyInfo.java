package com.weis.demo.dto;

import lombok.Data;

/**
 * 问候意图的关键信息
 */
@Data
public class GreetingKeyInfo implements AIMessageKeyInfo {
    
    /**
     * 后续流程，固定值："END"
     */
    private String subsequentFlow = "END";
    
    /**
     * 生成的友好问候及引导语
     */
    private String reply;
}
