package com.weis.demo.agent.v2;

import com.weis.demo.agent.typedkey.Intent;
import com.weis.demo.dto.v2.IntentDTO;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface IntentAgent {

    @SystemMessage("""
            ## 1. 根据用户输入的文字，识别用户意图。
            
            **严格遵守以下规则**
            1.1. 用户意图必须是以下几种之一：GREETING、DISCOVER_BRAND、ITEM_SEARCH、OTHER。
            1.2. **ITEM_SEARCH意图判定规则**：只有当用户明确表达购买意向、寻找具体商品、或要求推荐特定商品时，才判定为ITEM_SEARCH。典型的明确表达包括：
               - 包含"买"、"找"、"推荐"、"看"、"挑选"等购买意向动词
               - 包含具体的商品品类（如"衬衫"、"连衣裙"、"外套"）和明确的穿着场景/人群
               - 用户有明确的购买目的，而非仅询问知识或建议
            1.3. **DISCOVER_BRAND意图判定规则**：用户询问关于品牌本身的信息，如历史、理念、故事、背景等。
            1.4. **OTHER意图判定规则**：以下情况应判定为OTHER：
               - 用户询问服装相关知识、穿搭建议、使用场景建议（如"滑雪需要穿什么外套"）
               - 用户询问售后服务、物流、退换货等非购物问题
               - 用户进行闲聊或提出与品牌/购物无关的问题
               - 用户表达感谢、告别等社交性话语
               - 意图不明确或无法归类的情况
            1.5. 如果判定用户意图为DISCOVER_BRAND，则根据用户输入生成用于RAG的查询语句，填入embeddingQuery字段
            1.6. 如果判定用户意图为ITEM_SEARCH，则根据用户输入生成用于检索商品的查询关键词，填入itemQueryKeyWords字段
            1.7. 输出必须是且仅是一个合法的JSON对象
            
            ## 2. 输出示例：
            2.1. GREETING
            {
              "intentType": "GREETING",
            }
            
            2.2. DISCOVER_BRAND
            {
              "intentType": "DISCOVER_BRAND",
              "embeddingQuery": "品牌创立时间、发展历程和历史背景"
            }
            
            2.3. ITEM_SEARCH
            {
              "intentType": "ITEM_SEARCH",
              "itemQueryKeyWords": "黑色 裤子 通勤"
            }
            
            2.4. OTHER
            {
              "intentType": "OTHER"
            }
            """)
    @Agent(description = "用户意图识别", typedOutputKey  = Intent.class)
    IntentDTO analyze(@UserMessage @V("consultation") String consultation);
}


