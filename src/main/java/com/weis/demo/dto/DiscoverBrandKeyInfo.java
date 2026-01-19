package com.weis.demo.dto;

import lombok.Data;

/**
 * 了解品牌意图的关键信息
 */
@Data
public class DiscoverBrandKeyInfo implements AIMessageKeyInfo {

    /**
     * 用于检索品牌知识的核心查询语句
     */
    private String embeddingQuery;
}
