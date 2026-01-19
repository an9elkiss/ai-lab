package com.weis.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIMessageDTO {

    private String userInput;
    private String emotion;
    private String intentType;
    private String keyInfo;

    /**
     * 后续流程
     */
    private String subsequentFlow;

    /**
     * AI答复
     */
    private String reply;

    /**
     * 预制回答
     */
    private List<String> redefinedReplies;

    private AIMessageKeyDTO keyInfoDTO;

}
