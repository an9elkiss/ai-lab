package com.weis.demo.agent.provider;

import com.weis.demo.dto.MemberProfileDTO;
import com.weis.demo.dto.MemoryIdInfoDTO;
import com.weis.demo.dto.command.MemberProfileFindCmd;
import com.weis.demo.memory.MemoryIdCreator;
import com.weis.demo.service.MemberProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoldMedalGuideSystemMessageProvider implements Function<Object, String> {

    private final MemoryIdCreator memoryIdCreator;

    private final MemberProfileService memberProfileService;
    
    @Override
    public String apply(Object memoryId) {
        
        // 构建用户画像信息
        String memberProfileInfo = buildMemberProfileInfo(memoryId);

        String systemPrompt = """
          1. 角色定位 (Identity)
          你是一位拥有 10 年经验的顶级品牌服装导购，名叫“灵动顾问”。你不仅精通时尚穿搭，还具备极高的情商，能够通过微妙的对话洞察用户的真实需求。
          你不仅是在卖衣服，更是在为客户提供一种理想的生活方式和自信的形象。
          
          2. 核心任务逻辑 (Step-by-Step Logic)
          在处理用户请求时，请遵循以下思维路径：
          
          分析用户输入的文字，判断用户当前处于哪个阶段：需求表达期、犹豫期、反馈期或成交期。
          槽位填充（关键信息检查）：
          检查是否具备搜索所需的必要条件：性别、穿着场合（如面试、度假）、预算区间。
          原则： 如果缺失关键信息，不要盲目推荐，要优雅地提问，每次提问不超过 2 个问题。
          
          3. 销售技巧与话术指引 (Interaction Guidelines)
          专业穿搭建议： 不要只给商品链接。要解释“为什么选这个”，例如：“这款高腰直筒裤能很好地修饰腿型，搭配您刚才看中的那件短款西装，能从视觉上拉长比例。”
          同理心反馈： 用户嫌贵时，不要尴尬，要强调“质价比”或推荐折扣款：“我完全理解您对预算的考虑。这款虽然价格稍高，但它是 100% 桑蚕丝材质，利用率非常高。或者，您可以看看这款类似剪裁的特惠款？”
     
          4. 严格约束 (Constraints)
          数量限制： 单次推荐商品数量为3件。
          回复字数尽量少于100字。
          结尾必须包含一个引导性问题，保持对话连贯。
          
          5. 输出格式规范：
          **你的输出必须是且仅是一个合法的JSON对象**
          JSON对象第一层只包含以下四个字段：
          5.1  userInput: (字符串) 记录用户本轮对话的原始输入。
          5.2  emotion: (字符串) 分析用户的情绪状态，候选值为 positive、neutral、negative、hesitant
          5.3  intentType: (字符串) 判断用户的核心意图。意图仅限于以下类型：
              ◦   greeting: 问候。
              ◦   discover_brand: 了解品牌故事、理念等。
              ◦   item_search: 商品搜索。
              ◦   retrieval_augmented_generation: 根据事实进行回答。
              ◦   other: 除以上3种类型之外的其他意图。
          
              **注意**retrieval_augmented_generation意图判定规范：
              用户消息中必须包含“回答时基于以下事实:”这样的标记，才能判定为 retrieval_augmented_generation意图。
          
          5.4  keyInfo: (对象) 根据intent_type填充对应的关键处理信息，结构如下：
              ◦   greeting意图:
                  {
                    "subsequentFlow": "END",
                    "reply": "你生成的友好问候及引导语"
                  }
          
              ◦   other意图:
                  {
                    "subsequentFlow": "END",
                    "reply": "根据你的角色定位生成合理的回复"
                  }
          
              ◦   retrieval_augmented_generation意图:
                  {
                    "subsequentFlow": "END",
                    "reply": "根据你的角色定位生成合理的回复"
                  }
          
              ◦   discover_brand意图:
                  {
                    "subsequentFlow": "RAG",
                    "knowledgeBase": "brand_corpus", // 固定值
                    "embeddingQuery": "你提炼的、用于检索品牌知识的核心查询语句"
                  }
          
              ◦   item_search意图:
                  {
                    "subsequentFlow": "END",
                    "reply": "根据你的角色定位生成合理的回复"
                    "searchResult": JSON对象
                  }
          
          对话示例：
          • 用户说：“你好！”
          • 输出应类似于：
              {
                "userInput": "你好！",
                "emotion": "positive",
                "intentType": "greeting",
                "keyInfo": {
                  "subsequentFlow": "END",
                  "reply": "您好！我是您的专属时尚顾问“灵动顾问”。很高兴为您服务。今天想看看什么风格的衣物呢？"
                }
              }
          
          6. """ + memberProfileInfo;
        
        return systemPrompt;
    }
    
    /**
     * 构建会员画像信息
     * 
     * @param memoryId 内存ID
     * @return 会员画像信息字符串
     */
    private String buildMemberProfileInfo(Object memoryId) {
        try {
            // 1. memoryId解析成MemoryIdInfo
            if (memoryId == null || !(memoryId instanceof String)) {
                log.warn("MemoryId 为空或格式不正确: {}", memoryId);
                return "用户画像信息: 暂无用户信息";
            }
            
            String memoryIdStr = (String) memoryId;
            MemoryIdInfoDTO memoryIdInfo = memoryIdCreator.parseMemoryId(memoryIdStr);
            log.warn("解析 MemoryId 成功: {}", memoryIdInfo);
            
            // 2. 获取MemberProfile
            MemberProfileFindCmd cmd = new MemberProfileFindCmd();
            cmd.setStoreId(memoryIdInfo.getStoreId());
            cmd.setMemberId(memoryIdInfo.getMemberId());
            
            MemberProfileDTO memberProfile = memberProfileService.find(cmd);
            log.warn("获取会员档案成功: {}", memberProfile.getNickname());
            
            // 3. 将用户画像添加到系统提示词中
            return buildProfileText(memberProfile);
            
        } catch (Exception e) {
            log.error("构建会员画像信息失败", e);
            return "用户画像信息: 暂无用户信息";
        }
    }
    
    /**
     * 构建用户画像文本
     * 
     * @param profile 会员档案
     * @return 格式化的用户画像文本
     */
    private String buildProfileText(MemberProfileDTO profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户画像信息 (User Profile)\n");
        sb.append("          当前服务的用户信息如下：\n");
        sb.append("          • 用户昵称: ").append(profile.getNickname()).append("\n");
        sb.append("          • 会员等级: ").append(getMemberLevelText(profile.getLevel())).append("\n");
        sb.append("          • 积分余额: ").append(profile.getPoints()).append("分\n");
        sb.append("          • 会员状态: ").append(getMemberStatusText(profile.getStatus())).append("\n");
        
        if (profile.getTags() != null && !profile.getTags().isEmpty()) {
            sb.append("          • 用户标签: ").append(profile.getTags()).append("\n");
        }
        
        if (profile.getRemark() != null && !profile.getRemark().isEmpty()) {
            sb.append("          • 备注信息: ").append(profile.getRemark()).append("\n");
        }
        
        // 根据用户等级和标签提供个性化服务建议
        sb.append("          • 服务建议: ").append(getServiceSuggestion(profile)).append("\n");
        
        return sb.toString();
    }
    
    /**
     * 获取会员等级文本
     */
    private String getMemberLevelText(Integer level) {
        if (level == null) return "普通会员";
        return switch (level) {
            case 1 -> "普通会员";
            case 2 -> "银卡会员";
            case 3 -> "金卡会员";
            case 4 -> "钻石会员";
            case 5 -> "至尊会员";
            default -> "会员等级" + level;
        };
    }
    
    /**
     * 获取会员状态文本
     */
    private String getMemberStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "正常";
            case 2 -> "冻结";
            case 3 -> "注销";
            default -> "状态" + status;
        };
    }
    
    /**
     * 根据用户画像生成服务建议
     */
    private String getServiceSuggestion(MemberProfileDTO profile) {
        StringBuilder suggestion = new StringBuilder();
        
        // 根据会员等级调整服务策略
        if (profile.getLevel() != null && profile.getLevel() >= 4) {
            suggestion.append("VIP客户，提供专属优质服务");
        } else if (profile.getLevel() != null && profile.getLevel() >= 2) {
            suggestion.append("老客户，可推荐会员专享商品");
        } else {
            suggestion.append("新客户，注重引导和教育");
        }
        
        // 根据积分情况调整推荐策略
        if (profile.getPoints() != null && profile.getPoints() > 1000) {
            suggestion.append("，积分充足可推荐积分兑换商品");
        }
        
        // 根据标签调整服务方式
        if (profile.getTags() != null) {
            if (profile.getTags().contains("VIP")) {
                suggestion.append("，重点关注个性化需求");
            }
            if (profile.getTags().contains("新用户")) {
                suggestion.append("，耐心介绍品牌和产品特色");
            }
            if (profile.getTags().contains("活跃用户")) {
                suggestion.append("，可推荐新品和热销商品");
            }
        }
        
        return suggestion.toString();
    }
}
