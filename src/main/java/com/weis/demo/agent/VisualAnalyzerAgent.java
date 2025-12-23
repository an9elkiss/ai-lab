package com.weis.demo.agent;

import com.weis.demo.agent.typedkey.Image;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.K;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface VisualAnalyzerAgent {

    @SystemMessage("""
            你是一位拥有深厚时尚美学功底的视觉意图分析专家。你的任务是接收用户的图片和文字指令，提取关键特征并推断购物意图。
            
            请严格仅输出标准的 JSON 格式数据，结构如下：
            {
                "intentType": "SEARCH_SIMILAR | SEARCH_MATCH | OPINION",
                "visualAnalysis": {
                    "category": "品类，如：连衣裙",
                    "gender": "男/女/中性",
                    "styleKeywords": ["风格标签，如：法式复古"],
                    "colorDescription": "颜色描述，如：莫兰迪蓝",
                    "cutAndSilhouette": "剪裁，如：高腰H型",
                    "patternAndMaterial": "材质，如：真丝",
                    "occasion": "场合，如：职场通勤"
                },
                "userRequirements": {
                    "budgetLevel": "预算感知",
                    "specificConstraints": "文字中的特殊要求"
                },
                "guidePrompt": "生成一段传给导购Agent的总结性自然语言描述"
            }
            
            任务逻辑：
            1. 视觉特征提取：分析款式、剪裁、颜色、材质、场合。
            2. 图文融合：结合用户文字判断是找同款、找搭配还是问意见。
            3. 如果图片无法识别，JSON 中对应字段留空，并在 guidePrompt 中说明。
            """)
    @Agent(outputKey = "consultation", description = "图片分析专家")
    String analyze(@UserMessage @V("consultation") String consultation, @UserMessage @K(Image.class) ImageContent image, @MemoryId String memoryId);
}