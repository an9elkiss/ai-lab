# Research & Technical Decisions

**Feature**: Brand Clothing Intelligent Shopping Assistant Robot  
**Branch**: `002-smart-sales-guide`  
**Date**: 2026-01-06  
**Status**: Phase 0 Complete

## Overview

本文档记录针对智能导购机器人项目中未决技术选型和架构决策的研究结果。所有 `NEEDS CLARIFICATION` 项已通过技术调研和最佳实践分析得到解决。

---

## 1. 关系型数据库选型（Agent Memory 持久化后端）

### Decision
**PostgreSQL/MySQL 8.0+**（作为 LangChain4j Agent Memory 的持久化后端）

### Rationale
1. **LangChain4j Agent Memory 支持**: LangChain4j 提供数据库持久化后端，支持 PostgreSQL 和 MySQL
2. **用户画像持久化**: 用户画像和偏好标签需要跨会话保留，使用关系型数据库提供可靠持久化
3. **Spring Boot 生态集成度高**: Spring Data JPA 原生支持，与项目技术栈无缝衔接
4. **JSON 支持**: MySQL 8.0+ / PostgreSQL 的 JSON 数据类型支持用户画像中的动态标签字段（如偏好、历史行为）
5. **广泛采用**: 社区活跃，运维工具成熟，团队熟悉度高

### Alternatives Considered
- **纯内存存储**: 无法实现跨会话的用户画像恢复
- **MongoDB**: 无需事务的场景适合，但 LangChain4j Agent Memory 对关系型数据库支持更好
- **Redis 持久化**: 适合缓存，但不适合长期数据存储

### Implementation Notes
- LangChain4j Agent Memory 使用关系型数据库作为持久化后端，存储用户画像和偏好标签
- 会话上下文仅在内存中维护，会话结束后清空，不持久化
- 库存数据通过 InventoryCheckTool 封装外部 API 调用获取，不在本项目维护主数据
- **不涉及订单数据**：加购操作通过 API 调用电商平台，本系统仅记录推荐和反馈

---

## 2. Contract Testing 方案

### Decision
**REST Assured + WireMock + Pact (可选)**

### Rationale
1. **REST Assured**: 用于 API 集成测试，提供流畅的 DSL 验证 REST 接口行为，与 Spring Boot Test 无缝集成
2. **WireMock**: 模拟外部依赖（如电商平台 API），支持录制/回放模式，便于隔离测试
3. **Pact (可选)**: 若前端独立开发，使用 Pact 验证前后端 API 契约，支持 TypeScript/JavaScript
4. **纯 Spring Boot 生态**: 不依赖 Spring Cloud，适合单体或简单分布式架构

### Alternatives Considered
- **Spring Cloud Contract**: 功能强大但引入 Spring Cloud 依赖，不符合项目要求
- **纯手工 Mock**: 维护成本高，接口变更时容易遗漏
- **Postman Collection Tests**: 适合手动测试，但不适合 CI/CD 自动化流程

### Implementation Notes
- 使用 `@SpringBootTest` + REST Assured 进行端到端 API 测试
- 工具层（Agent Tools）使用 Mockito 进行单元测试
- 外部 API 调用使用 WireMock 模拟（如库存查询、加购接口）

---

## 3. 预估并发用户数

### Decision
**初期支持 500 并发会话，峰值设计 2000 并发**

### Rationale
1. **业务场景分析**: 服装品牌电商典型日活（DAU）在 1-5 万量级，按 5% 同时使用导购机器人估算，峰值约 500-2500 人
2. **技术储备**: Spring Boot 配合线程池和响应式编程（WebFlux），单实例可支持 500-1000 长连接，水平扩展至 2-3 实例满足峰值
3. **成本平衡**: 初期避免过度设计，通过监控和弹性伸缩应对流量增长

### Alternatives Considered
- **10000+ 并发**: 过早优化，需引入消息队列和复杂的会话分片机制
- **100 并发**: 无法满足营销活动（如大促）流量尖峰

---

## 4. 商品 SKU 数量预估

### Decision
**初期 10,000 SKU，扩展设计支持 100,000 SKU（通过外部 API 提供）**

### Rationale
1. **品牌规模推断**: 单一服装品牌通常维护 5000-20000 SKU（包含季节款、颜色尺码组合）
2. **外部 API 架构**: 商品信息通过 ProductSearchTool 封装外部 API 调用获取，不存储在系统内部
3. **性能考虑**: 外部 API 响应时间需控制在 <500ms，支持按属性过滤和排序
4. **扩展性**: 通过外部服务管理商品数据，系统无需维护商品主数据，支持未来扩展至多品牌场景

### Alternatives Considered
- **1000 SKU**: 不符合实际品牌商品规模
- **1000000 SKU**: 外部 API 需支持分页和高效查询，初期验证可行性
- **本地存储商品数据**: 增加系统复杂度，不符合架构设计（商品信息通过 Tool 提供）

---

## 5. Lookbook 搭配库规模

### Decision
**初期 500-1000 套搭配方案（通过外部 API 提供）**

### Rationale
1. **人工策划成本**: 每套 Lookbook 需设计师策划，初期每季度（3 个月）产出 100-200 套为合理速度
2. **覆盖度计算**: 按 5 大风格（正式/休闲/运动/通勤/晚宴）× 4 季节 × 20 场景 = 400 基础组合，考虑变体约 1000 套
3. **外部 API 架构**: 搭配信息通过 LookbookSearchTool 封装外部 API 调用获取，不存储在系统内部
4. **检索性能**: 外部 API 需支持基于风格、场景的检索，响应时间需控制在 <500ms

### Alternatives Considered
- **10000+ Lookbook**: 内容生产跟不上，且用户偏好聚类有限，存在大量冗余
- **<100 Lookbook**: 覆盖场景不足，推荐单一
- **本地存储 Lookbook**: 增加系统复杂度，不符合架构设计（搭配信息通过 Tool 提供）

---

## 6. 对话历史保留策略

### Decision
**短期记忆（Session 内）+ 长期记忆（用户画像）**

### Rationale
1. **短期记忆**: 
   - 当前会话的上下文（Intent、Entity、反馈）保存在 Redis，Session 过期时间 30 分钟
   - LangChain4j 的 `ChatMemory` 组件管理最近 10 轮对话（约 5 个来回）
2. **长期记忆**: 
   - 提取关键偏好（颜色/风格/尺码）存入 PostgreSQL 的 `UserProfile` 表
   - 不保存完整对话文本，只保留结构化标签（GDPR 合规）
3. **数据合规**: 用户可通过 API 请求删除个人对话记录

### Alternatives Considered
- **全量日志保留**: 隐私风险高，存储成本大
- **纯无状态**: 无法实现"记住我的偏好"等个性化功能

---

## 7. Embedding 模型选型（Qwen/Ali Qianwen 具体版本）

### Decision
**通义千问 Embedding V2 (Qwen-Embedding-V2)**

### Rationale
1. **多模态支持**: 支持文本和图片输入，满足"用户上传穿搭图片"需求
2. **维度可选**: 支持 768/1024/1536 维，权衡精度与性能选择 768 维
3. **阿里云生态**: 与阿里云 PAI 和 Elasticsearch 服务集成度高，运维成本低
4. **中文优化**: 针对中文语义场景（如"领口"、"版型"）表现优于通用模型

### Alternatives Considered
- **OpenAI text-embedding-3**: 效果好但 API 调用成本高，且海外服务稳定性存疑
- **BERT 自训练**: 需标注大量服装领域数据，周期长

---

## 8. LangChain4j Tool 设计模式

### Decision
**工具类（Tool）+ 服务层（Service）分离**

### Rationale
1. **Agent 工具定义**: 
   ```java
   @Tool("Search products by attributes")
   public List<Product> searchProducts(
       @ToolParam("category") String category,
       @ToolParam("color") String color,
       @ToolParam("maxPrice") Double maxPrice
   ) {
       return productRetrievalService.search(category, color, maxPrice);
   }
   ```
2. **服务层复用**: `ProductRetrievalService` 可被 Agent 和 REST API 同时调用，避免逻辑重复
3. **测试友好**: 服务层可独立单元测试，工具层仅需验证参数映射

### Alternatives Considered
- **工具内嵌业务逻辑**: 导致 Agent 工具类膨胀，难以维护
- **纯 REST API 调用**: Agent 需额外处理 HTTP 序列化，性能损耗大

---

## 9. 视觉相似性检索实现方案

### Decision
**CLIP 模型作为独立服务（API）+ ProductSearchTool 封装调用**

### Rationale
1. **CLIP（Contrastive Language-Image Pre-Training）**: 
   - 将商品图片和用户上传图片转为统一向量空间（768 维）
   - 支持跨模态检索（文本描述匹配图片）
2. **独立服务架构**: 
   - CLIP 模型作为独立服务（API）部署，ProductSearchTool 封装服务调用
   - 支持水平扩展，避免模型推理阻塞 Agent 主流程
3. **差异性检索**: 
   - 用户拒绝某商品时，通过 CLIP 服务生成向量，ProductSearchTool 调用外部商品 API 进行相似性检索
   - 检索与其向量余弦距离 >0.5 的商品（即视觉差异大）

### Alternatives Considered
- **Faiss**: 性能更强，但需独立部署和维护，增加系统复杂度
- **纯标签匹配**: 无法处理"领口"等缺失标签场景
- **内置 CLIP 模型**: 模型推理会阻塞 Agent 主流程，影响响应时间

---

## 10. Elasticsearch 用途和销售政策知识库

### Decision
**Elasticsearch 作为向量数据库，仅用于销售政策知识库**

### Rationale
1. **架构分离**: 
   - 商品信息和搭配信息通过 Tool 封装外部 API 提供，不存储在系统内部
   - Elasticsearch 专注于销售政策知识库的向量检索
2. **销售政策知识库内容**: 
   - 包含销售话术模板和推荐策略（如价格敏感用户策略、风格偏好策略）
   - 用于生成个性化的推荐理由和销售话术
3. **访问方式**: 
   - Agent 直接调用 Elasticsearch 向量检索 API 查询销售政策知识库
   - 不通过 Tool 封装，简化架构
4. **向量化**: 
   - 使用 Qwen Embedding V2 (768维) 对销售政策知识进行向量化
   - 支持基于语义相似度的知识检索

### Alternatives Considered
- **Elasticsearch 存储商品数据**: 不符合架构设计（商品信息通过 Tool 提供）
- **通过 Tool 封装知识库访问**: 增加不必要的抽象层，Agent 直接调用更高效
- **使用其他向量数据库**: Elasticsearch 符合 Constitution 要求，且团队熟悉度高

### Implementation Notes
- 销售政策知识库包含销售话术模板和推荐策略
- Agent 在生成推荐理由时，通过向量检索查询相关知识
- 知识库支持动态更新，新增策略和话术可实时生效

---

## 11. 会话状态机设计

### Decision
**有限状态机（FSM）+ LangChain4j Agent 决策**

### Rationale
1. **状态定义**: 
   - `GREETING` → `INTENT_RECOGNITION` → `CLARIFICATION` → `RECOMMENDATION` → `FEEDBACK` → `CONVERSION`
2. **状态转移**: 
   - Agent 根据用户输入和工具调用结果决定下一状态
   - 允许跳转（如用户直接说"我要这件，M 码"可跳过多轮澄清）
3. **中间件拦截**: 在状态转移前执行校验（如"是否超过 10 轮对话"触发兜底策略）

### Alternatives Considered
- **纯 Prompt 引导**: 依赖 LLM 自行维护状态，不可控且 Token 消耗大
- **硬编码规则引擎**: 无法应对复杂对话分支，扩展性差

---

## Technology Stack Summary

| 组件 | 选型 | 版本 | 用途 |
|------|------|------|------|
| 关系型数据库 | PostgreSQL/MySQL | 8.0+ | Agent Memory 持久化后端（用户画像） |
| 向量数据库 | Elasticsearch | 8.x | 销售政策知识库（向量检索） |
| Embedding 模型 | 通义千问 Embedding V2 | Qwen-Embedding-V2 (768维) | 销售政策知识库向量化 |
| 视觉模型 | CLIP | OpenAI CLIP-ViT-B/32 | 独立服务（API），用于商品图片向量生成 |
| Contract Testing | REST Assured + WireMock + Pact (可选) | Latest | API 契约测试 |
| 缓存/会话存储 | Redis | 7.x | 会话上下文缓存 |
| 消息队列（可选） | RabbitMQ | 3.12+ (若需异步处理) | 异步处理（可选） |

---

## Next Steps

1. ✅ 所有 `NEEDS CLARIFICATION` 已解决，可进入 **Phase 1: 设计与契约生成**
2. Phase 1 输出: `data-model.md`, `contracts/`, `quickstart.md`
3. 建议优先实现 POC: 意图识别 + 单轮推荐（不含 Lookbook），验证 LangChain4j + Elasticsearch 集成

---

**研究完成日期**: 2026-01-06  
**审核状态**: Pending (需技术负责人确认并发用户数和 SKU 规模假设)
