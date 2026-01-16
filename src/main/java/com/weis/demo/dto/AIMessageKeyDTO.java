package com.weis.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIMessageKeyDTO {

    /**
     * 后续流程
     */
    private String subsequentFlow;

    /**
     * 根据角色定位生成的合理回复
     */
    private String reply;

    /**
     * 预制回答
     */
    private List<String> redefinedReplies;

    /**
     * 搜索结果数据
     */
    private List<ItemDTO> searchResult;

    /**
     * 知识库名称
     */
    private String knowledgeBase;

    /**
     * 用于检索品牌知识的核心查询语句
     */
    private String embeddingQuery;
}
