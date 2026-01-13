package com.weis.demo.dto;

import lombok.Data;

/**
 * 了解品牌意图的关键信息
 */
@Data
public class DiscoverBrandKeyInfo implements AIMessageKeyInfo {
    
    /**
     * 后续流程，固定值："RAG"
     */
    private String subsequentFlow = "RAG";
    
    /**
     * 知识库名称，固定值："brand_corpus"
     */
    private String knowledgeBase = "brand_corpus";
    
    /**
     * 用于检索品牌知识的核心查询语句
     */
    private String embeddingQuery;
}
