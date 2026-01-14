package com.weis.demo.dto;

import lombok.Data;

/**
 * 商品搜索意图的关键信息
 */
@Data
public class ItemSearchKeyInfo implements AIMessageKeyInfo {

    /**
     * 后续流程，固定值："END"
     */
    private String subsequentFlow = "END";

    /**
     * 根据角色定位生成的合理回复
     */
    private String reply;

    /**
     * 搜索结果数据
     */
    private String searchResult;
}