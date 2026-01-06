# Data Model: Brand Clothing Intelligent Shopping Assistant Robot

**Feature**: Brand Clothing Intelligent Shopping Assistant Robot  
**Branch**: `002-smart-sales-guide`  
**Date**: 2026-01-06  
**Status**: Phase 1 Design

## Overview

本文档定义智能导购机器人系统的核心数据模型。根据架构设计，用户画像通过 LangChain4j Agent Memory 管理，商品和搭配信息通过外部 API 提供，销售政策知识库存储在 Elasticsearch。

---

## 1. User Profile (用户画像)

**存储方式**: LangChain4j Agent Memory 持久化后端（PostgreSQL/MySQL）  
**生命周期**: 跨会话保留（登录用户），会话内（匿名用户）

### Fields

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `userId` | String | PK, Optional | 用户ID（登录用户），匿名用户为 null |
| `sessionId` | String | PK, Required | 会话ID/Token，唯一标识会话 |
| `preferences` | JSON | Optional | 用户偏好标签（颜色、风格、品类等） |
| `stylePreferences` | JSON | Optional | 风格偏好（正式/休闲/运动等） |
| `colorPreferences` | JSON | Optional | 颜色偏好（红色、蓝色等） |
| `sizeProfile` | JSON | Optional | 尺码档案（身高、体重、常用尺码） |
| `budgetRange` | JSON | Optional | 预算范围（min, max） |
| `browsingHistory` | JSON | Optional | 历史浏览记录（商品ID列表，最多保留100条） |
| `feedbackHistory` | JSON | Optional | 反馈历史（正反馈/负反馈的商品ID和原因） |
| `createdAt` | Timestamp | Required | 创建时间 |
| `updatedAt` | Timestamp | Required | 更新时间 |

### Relationships

- **1:N** 与 `DialogueSession`（一个用户可以有多个会话，但会话上下文不持久化）

### Validation Rules

- `preferences` JSON 结构: `{"categories": ["dress", "jacket"], "colors": ["red", "blue"], "styles": ["formal", "casual"]}`
- `sizeProfile` JSON 结构: `{"height": 165, "weight": 55, "usualSize": "M"}`
- `budgetRange` JSON 结构: `{"min": 100, "max": 1000, "currency": "CNY"}`

### State Transitions

- **创建**: 用户首次访问 → 创建匿名 UserProfile（仅 sessionId）
- **关联**: 用户登录 → 将 sessionId 关联到 userId，恢复历史偏好
- **更新**: 每次对话交互 → 更新 preferences、browsingHistory、feedbackHistory

---

## 2. Dialogue Session (会话上下文)

**存储方式**: LangChain4j Agent Memory（内存），Redis 缓存  
**生命周期**: 仅在当前会话内维护，会话结束后清空

### Fields

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `sessionId` | String | PK, Required | 会话ID/Token |
| `userId` | String | Optional | 用户ID（登录用户） |
| `currentState` | Enum | Required | 当前状态（GREETING, INTENT_RECOGNITION, CLARIFICATION, RECOMMENDATION, FEEDBACK, CONVERSION） |
| `conversationHistory` | List<Message> | Required | 对话历史（最近10轮，约5个来回） |
| `intent` | JSON | Optional | 当前识别的意图（品类、场景、风格等） |
| `missingAttributes` | List<String> | Optional | 缺失的关键属性（需追问） |
| `excludedProductIds` | List<String> | Optional | 已排除的商品ID（负反馈） |
| `excludedAttributes` | JSON | Optional | 已排除的属性（颜色、价格等） |
| `createdAt` | Timestamp | Required | 创建时间 |
| `lastActivityAt` | Timestamp | Required | 最后活动时间 |
| `ttl` | Integer | Required | 过期时间（秒），默认1800（30分钟） |

### Message Structure

```json
{
  "role": "user|assistant",
  "content": "消息内容",
  "timestamp": "2026-01-06T10:00:00Z",
  "metadata": {
    "intent": "购买连衣裙",
    "entities": ["品类:连衣裙", "颜色:红色"],
    "toolCalls": ["ProductSearchTool"]
  }
}
```

### State Transitions

- **GREETING** → **INTENT_RECOGNITION**: 用户发送第一条消息
- **INTENT_RECOGNITION** → **CLARIFICATION**: 检测到缺失关键信息
- **CLARIFICATION** → **RECOMMENDATION**: 信息收集完整
- **RECOMMENDATION** → **FEEDBACK**: 用户对推荐提出反馈
- **FEEDBACK** → **RECOMMENDATION**: 根据反馈重新推荐
- **RECOMMENDATION** → **CONVERSION**: 用户选择商品，进入加购流程
- 任意状态 → **CONVERSION**: 用户直接表达购买意向

---

## 3. Product (商品)

**存储方式**: 外部 API（通过 ProductSearchTool 提供）  
**生命周期**: 由外部系统管理

### Fields (API 返回结构)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `productId` | String | 商品ID |
| `name` | String | 商品名称 |
| `category` | String | 品类（dress, jacket, pants等） |
| `color` | String | 颜色 |
| `material` | String | 材质 |
| `size` | List<String> | 可用尺码列表 |
| `price` | Decimal | 价格 |
| `originalPrice` | Decimal | 原价（如有促销） |
| `images` | List<String> | 图片URL列表 |
| `imageEmbedding` | List<Float> | 图片向量（768维，CLIP生成） |
| `sellingPoints` | List<String> | 卖点列表 |
| `styleTags` | List<String> | 风格标签 |
| `scenarioTags` | List<String> | 场景标签（年会、日常等） |
| `stockStatus` | JSON | 库存状态（各尺码库存数量） |

### Relationships

- **N:M** 与 `Outfit`（一个商品可以出现在多个搭配中）

### Notes

- 商品数据不存储在系统内部，通过 ProductSearchTool 封装外部 API 调用获取
- 图片向量由 CLIP 模型服务生成，ProductSearchTool 调用 CLIP 服务获取向量

---

## 4. Outfit (Lookbook - 搭配)

**存储方式**: 外部 API（通过 LookbookSearchTool 提供）  
**生命周期**: 由外部系统管理

### Fields (API 返回结构)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `lookbookId` | String | 搭配ID |
| `name` | String | 搭配名称 |
| `productIds` | List<String> | 包含的商品ID列表（上衣、下装、配饰等） |
| `styleTags` | List<String> | 风格标签（正式、休闲、运动等） |
| `scenarioTags` | List<String> | 场景标签（年会、日常、通勤等） |
| `seasonTags` | List<String> | 季节标签（春夏秋冬） |
| `imageUrl` | String | 整体展示图URL |
| `description` | String | 搭配描述 |
| `price` | Decimal | 整套搭配总价 |

### Relationships

- **N:M** 与 `Product`（一个搭配包含多个商品）

### Notes

- 搭配数据不存储在系统内部，通过 LookbookSearchTool 封装外部 API 调用获取
- 优先推荐 Lookbook，用户选中后再针对单品确认尺码

---

## 5. Sales Policy (销售政策知识库)

**存储方式**: Elasticsearch（向量数据库）  
**生命周期**: 长期存储，支持动态更新

### Fields

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `policyId` | String | PK, Required | 政策ID |
| `type` | Enum | Required | 类型（SALES_PHRASE, RECOMMENDATION_STRATEGY） |
| `content` | Text | Required | 内容（销售话术或推荐策略） |
| `embedding` | Vector(768) | Required | 向量（Qwen Embedding V2生成） |
| `tags` | List<String> | Optional | 标签（价格敏感、风格偏好、场景等） |
| `priority` | Integer | Optional | 优先级（1-10，数字越大优先级越高） |
| `effectiveDate` | Date | Required | 生效日期 |
| `expiryDate` | Date | Optional | 失效日期 |
| `createdAt` | Timestamp | Required | 创建时间 |
| `updatedAt` | Timestamp | Required | 更新时间 |

### Types

- **SALES_PHRASE**: 销售话术模板，如"这款正红色非常衬肤色，剪裁适合晚宴场合"
- **RECOMMENDATION_STRATEGY**: 推荐策略，如"价格敏感用户策略"、"风格偏好策略"

### Index Structure (Elasticsearch)

```json
{
  "mappings": {
    "properties": {
      "policyId": {"type": "keyword"},
      "type": {"type": "keyword"},
      "content": {"type": "text", "analyzer": "ik_max_word"},
      "embedding": {
        "type": "dense_vector",
        "dims": 768,
        "index": true,
        "similarity": "cosine"
      },
      "tags": {"type": "keyword"},
      "priority": {"type": "integer"},
      "effectiveDate": {"type": "date"},
      "expiryDate": {"type": "date"}
    }
  }
}
```

### Query Pattern

Agent 直接调用 Elasticsearch 向量检索 API，使用 `knn` 查询：

```json
{
  "knn": {
    "field": "embedding",
    "query_vector": [0.1, 0.2, ...],
    "k": 5,
    "num_candidates": 100
  },
  "filter": {
    "bool": {
      "must": [
        {"term": {"type": "SALES_PHRASE"}},
        {"range": {"effectiveDate": {"lte": "now"}}}
      ]
    }
  }
}
```

---

## 6. External API Contracts

### ProductSearchTool API

**Endpoint**: `GET /api/products/search`  
**Parameters**:
- `category` (String, Optional): 品类
- `color` (String, Optional): 颜色
- `style` (String, Optional): 风格
- `minPrice` (Decimal, Optional): 最低价格
- `maxPrice` (Decimal, Optional): 最高价格
- `excludeProductIds` (List<String>, Optional): 排除的商品ID
- `excludeAttributes` (JSON, Optional): 排除的属性
- `limit` (Integer, Default: 10): 返回数量限制

**Response**: `List<Product>`

### LookbookSearchTool API

**Endpoint**: `GET /api/lookbooks/search`  
**Parameters**:
- `styleTags` (List<String>, Optional): 风格标签
- `scenarioTags` (List<String>, Optional): 场景标签
- `seasonTags` (List<String>, Optional): 季节标签
- `limit` (Integer, Default: 10): 返回数量限制

**Response**: `List<Outfit>`

### InventoryCheckTool API

**Endpoint**: `GET /api/inventory/check`  
**Parameters**:
- `productId` (String, Required): 商品ID
- `size` (String, Optional): 尺码

**Response**: `{"productId": "...", "size": "M", "stock": 5, "status": "in_stock"}`

### PromotionTool API

**Endpoint**: `GET /api/promotions/check`  
**Parameters**:
- `productId` (String, Required): 商品ID

**Response**: `{"productId": "...", "discount": 0.2, "promotionText": "限时8折"}`

### CLIP Service API

**Endpoint**: `POST /api/clip/embed`  
**Request Body**: `{"imageUrl": "..."}` 或 `{"imageBase64": "..."}`

**Response**: `{"embedding": [0.1, 0.2, ...], "dims": 768}`

---

## Data Flow Summary

1. **用户画像**: LangChain4j Agent Memory → PostgreSQL/MySQL（持久化）
2. **会话上下文**: LangChain4j Agent Memory → Redis（缓存，30分钟TTL）
3. **商品信息**: ProductSearchTool → 外部 API → 返回商品数据
4. **搭配信息**: LookbookSearchTool → 外部 API → 返回搭配数据
5. **销售政策**: Agent → Elasticsearch 向量检索 API → 返回相关知识
6. **图片向量**: ProductSearchTool → CLIP 服务 API → 返回向量

---

**文档完成日期**: 2026-01-06  
**审核状态**: Pending
