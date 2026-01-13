package com.weis.demo.dto;

import lombok.Data;

/**
 * 搜索商品意图的关键信息
 */
@Data
public class SearchProductKeyInfo implements AIMessageKeyInfo {
    
    /**
     * 后续流程，固定值："TOOL"
     */
    private String subsequentFlow = "TOOL";
    
    /**
     * 工具名称
     */
    private String toolName;
    
    /**
     * 搜索参数（尽可能从用户输入中提取并填充）
     */
    private SearchParams params;
    
    /**
     * 商品搜索参数
     */
    @Data
    public static class SearchParams {
        /**
         * 主要品类关键词，如'连衣裙'
         */
        private String keyWord;
        
        /**
         * 场景
         */
        private String scene;
        
        /**
         * 性别（male/female）
         */
        private String gender;
        
        /**
         * 风格
         */
        private String style;
        
        /**
         * 颜色
         */
        private String color;
        
        /**
         * 价格区间
         */
        private String priceRange;
    }
}
