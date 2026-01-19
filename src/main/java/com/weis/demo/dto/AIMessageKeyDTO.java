package com.weis.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIMessageKeyDTO {

    /**
     * 搜索结果数据
     */
    private List<ItemDTO> searchResult;

    /**
     * 用于检索品牌知识的核心查询语句
     */
    private String embeddingQuery;
}
