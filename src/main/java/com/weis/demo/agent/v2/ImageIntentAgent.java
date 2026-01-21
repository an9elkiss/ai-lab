package com.weis.demo.agent.v2;

import com.weis.demo.agent.typedkey.Image;
import com.weis.demo.agent.typedkey.Intent;
import com.weis.demo.dto.v2.IntentDTO;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.K;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ImageIntentAgent {

    String SYSTEM_MESSAGE = """
            ## 1. 根据用户输入的文字和图片，识别用户意图。
            
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
            1.5. 如果判定用户意图为DISCOVER_BRAND，则根据用户输入结合图片生成用于RAG的查询语句，填入embeddingQuery字段
            1.6. 如果判定用户意图为ITEM_SEARCH，则根据用户输入结合图片生成用于检索商品的查询关键词，填入itemQueryKeyWords字段
            1.7. 输出必须是且仅是一个合法的JSON对象
            
            ## 2. 图片处理逻辑
            结合用户输入的文字，生成一段简洁、准确、客观的图片内容描述。若用户输入的文字为空，则进行纯粹的图片描述。
               - 专注视觉描述：仔细观察图片，描述其中的主体元素（人物、服装、物品等）、背景、构图、排版、颜色、风格等视觉信息。
               - 结合文字补充：若提供了文字，需将文字信息与图片内容进行关联解读，但描述核心必须是图片本身。例如，若文字是“这条裙子”，则需明确指出图片中哪件物品是裙子并描述其视觉特征。
               - 补充关键信息：对于图片主要内容，如能明确推断（如图中人物可能的年龄、身形，服装明显的风格、品类、场合），应在描述中补充。但避免过度主观猜测。
               - 简洁与格式：描述需简洁，总字数不超过256字。输出应仅为描述文本，无需额外说明、问候或解释。
            
            ## 3. 输出示例：
            3.1. GREETING
            {
              "intentType": "GREETING",
              "imageContent": "如果用户提供了图片，在此进行描述"
            }
            
            3.2. DISCOVER_BRAND
            {
              "intentType": "DISCOVER_BRAND",
              "embeddingQuery": "品牌创立时间、发展历程和历史背景",
              "imageContent": "如果用户提供了图片，在此进行描述"
            }
            
            3.3. ITEM_SEARCH
            {
              "intentType": "ITEM_SEARCH",
              "itemQueryKeyWords": "黑色 裤子 通勤",
              "imageContent": "如果用户提供了图片，在此进行描述"
            }
            
            3.4. OTHER
            {
              "intentType": "OTHER",
              "imageContent": "如果用户提供了图片，在此进行描述"
            }
            """;

    @Agent(description = "图片分析专家", typedOutputKey  = Intent.class)
    IntentDTO analyze(@UserMessage @V("consultation") String consultation,
                      @UserMessage @K(Image.class) ImageContent image,
                      @MemoryId String memoryId);
}