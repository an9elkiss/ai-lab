package com.weis.demo.dto.constant;

import com.weis.demo.dto.*;

/**
 * 用户意图类型枚举
 */
public enum IntentType {
    /**
     * 问候
     */
    GREETING("greeting", "问候", GreetingKeyInfo.class),
    
    /**
     * 了解品牌故事、理念等
     */
    DISCOVER_BRAND("discover_brand", "了解品牌故事、理念等", DiscoverBrandKeyInfo.class),
    
    /**
     * 搜索商品
     */
    SEARCH_PRODUCT("search_product", "搜索商品", SearchProductKeyInfo.class),
    
    /**
     * 检索增强生成
     */
    RETRIEVAL_AUGMENTED_GENERATION("retrieval_augmented_generation", "检索增强生成", RAGKeyInfo.class),
    
    /**
     * 除以上类型之外的其他意图
     */
    OTHER("other", "其他意图", OtherKeyInfo.class);
    
    private final String code;
    private final String description;
    private final Class<? extends AIMessageKeyInfo> keyInfoClass;
    
    IntentType(String code, String description, Class<? extends AIMessageKeyInfo> keyInfoClass) {
        this.code = code;
        this.description = description;
        this.keyInfoClass = keyInfoClass;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Class<? extends AIMessageKeyInfo> getKeyInfoClass() {
        return keyInfoClass;
    }
    
    /**
     * 根据code获取枚举
     */
    public static IntentType fromCode(String code) {
        for (IntentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
