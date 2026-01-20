package com.weis.demo.dto.v2;

import com.weis.demo.dto.constant.IntentTypeV2;
import dev.langchain4j.model.output.structured.Description;
import lombok.Data;


@Data
public class IntentDTO {

    @Description("用户的意图")
    private IntentTypeV2 intentType;

    @Description("用于检索品牌知识的核心查询语句")
    private String embeddingQuery;

    @Description("用于检索商品的查询关键词")
    private String itemQueryKeyWords;

}
