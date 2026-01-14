package com.weis.demo.dto.command;

import lombok.Data;

/**
 * 商品搜索命令对象
 */
@Data
public class ItemSearchCmd {
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
