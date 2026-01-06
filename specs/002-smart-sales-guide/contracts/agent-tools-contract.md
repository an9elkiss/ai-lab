# Agent Tools Contract Specification

**Feature**: Smart Sales Guide Agent Tools  
**Version**: 1.0  
**Date**: 2026-01-06

## Overview

本文档定义 LangChain4j Agent 可调用的工具（Tools）接口规范。所有工具必须遵循以下原则：
1. **参数明确**: 使用 `@ToolParam` 注解明确参数语义
2. **返回结构化**: 返回 POJO 对象或 `List<T>`，避免返回纯字符串
3. **异常处理**: 抛出业务异常时，Agent 应能理解错误并向用户解释

---

## Tool 1: ProductSearchTool

### Purpose
根据用户需求属性（品类、颜色、风格等）检索商品

### Java Signature

```java
@Tool("Search products by user requirements")
public List<Product> searchProducts(
    @ToolParam(value = "category", required = false) String category,
    @ToolParam(value = "colors", required = false) List<String> colors,
    @ToolParam(value = "styleTags", required = false) List<String> styleTags,
    @ToolParam(value = "sceneTags", required = false) List<String> sceneTags,
    @ToolParam(value = "minPrice", required = false) Double minPrice,
    @ToolParam(value = "maxPrice", required = false) Double maxPrice,
    @ToolParam(value = "excludeProductIds", required = false) List<String> excludeProductIds,
    @ToolParam(value = "limit", required = false, defaultValue = "10") Integer limit
);
```

### Parameters

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `category` | String | 否 | 商品品类 | "连衣裙" |
| `colors` | List<String> | 否 | 颜色筛选（AND逻辑） | ["红色", "黑色"] |
| `styleTags` | List<String> | 否 | 风格标签（OR逻辑） | ["优雅", "正式"] |
| `sceneTags` | List<String> | 否 | 场景标签（OR逻辑） | ["婚礼", "年会"] |
| `minPrice` | Double | 否 | 最低价格 | 200.0 |
| `maxPrice` | Double | 否 | 最高价格 | 1000.0 |
| `excludeProductIds` | List<String> | 否 | 排除的商品ID（处理负反馈） | ["prod-123"] |
| `limit` | Integer | 否 | 返回数量限制（默认10） | 5 |

### Return Type

```java
public class Product {
    private String id;
    private String name;
    private String category;
    private Double price;
    private Double originalPrice;
    private List<String> colors;
    private List<String> availableSizes;
    private List<String> styleTags;
    private String imageUrl;
    private Integer stockQuantity;
}
```

### Business Rules
1. 若无匹配结果，返回空列表（不抛异常）
2. 结果按销量和评分综合排序
3. 库存为 0 的商品不返回

### Example Agent Usage

```text
User: "我想找一件去参加婚礼的红色裙子，预算1000元以内"

Agent思考:
- category: "连衣裙"
- colors: ["红色"]
- sceneTags: ["婚礼"]
- maxPrice: 1000.0

Agent调用:
searchProducts(category="连衣裙", colors=["红色"], sceneTags=["婚礼"], maxPrice=1000.0, limit=5)
```

---

## Tool 2: LookbookSearchTool

### Purpose
根据风格和场景检索预设的整套搭配方案

### Java Signature

```java
@Tool("Search outfit lookbooks by style and scene")
public List<Lookbook> searchLookbooks(
    @ToolParam(value = "style", required = false) String style,
    @ToolParam(value = "scene", required = false) String scene,
    @ToolParam(value = "season", required = false) String season,
    @ToolParam(value = "limit", required = false, defaultValue = "5") Integer limit
);
```

### Parameters

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `style` | String | 否 | 风格（休闲/正式/通勤） | "通勤" |
| `scene` | String | 否 | 场景（婚礼/办公室） | "办公室" |
| `season` | String | 否 | 季节（春夏/秋冬） | "秋冬" |
| `limit` | Integer | 否 | 返回数量限制（默认5） | 3 |

### Return Type

```java
public class Lookbook {
    private String id;
    private String name;
    private String style;
    private String scene;
    private List<String> productIds;
    private String overallImageUrl;
    private String description;
}
```

### Business Rules
1. 优先推荐活跃状态的搭配（`is_active=true`）
2. 所有单品必须有库存
3. 按创建时间倒序（最新优先）

### Example Agent Usage

```text
User: "我需要一套适合上班穿的秋装"

Agent思考:
- style: "通勤"
- season: "秋冬"

Agent调用:
searchLookbooks(style="通勤", season="秋冬", limit=3)

Agent回复:
"我为您挑选了3套适合秋季通勤的搭配：
1. 职场OL套装（白衬衫+黑西裤）
2. 知性风毛衣搭配（驼色毛衣+半身裙）
..."
```

---

## Tool 3: SizeRecommendationTool

### Purpose
根据用户身高体重和商品版型推荐尺码

### Java Signature

```java
@Tool("Recommend size based on user body measurements")
public SizeRecommendation recommendSize(
    @ToolParam(value = "productId", required = true) String productId,
    @ToolParam(value = "height", required = false) Integer height,
    @ToolParam(value = "weight", required = false) Integer weight,
    @ToolParam(value = "userId", required = false) String userId
);
```

### Parameters

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `productId` | String | 是 | 目标商品ID |
| `height` | Integer | 否 | 身高（cm），若为空则从UserProfile读取 |
| `weight` | Integer | 否 | 体重（kg），若为空则从UserProfile读取 |
| `userId` | String | 否 | 用户ID（用于读取历史尺码记录） |

### Return Type

```java
public class SizeRecommendation {
    private String recommendedSize;    // "M"
    private String reason;             // "根据您的身高165cm和体重55kg，建议选择M码"
    private String fitNote;            // "此款版型偏小，若喜欢宽松可选L码"
    private List<String> availableSizes; // ["S", "M", "L"]
}
```

### Business Rules
1. 若用户未提供身高体重且 `UserProfile` 也无记录，返回 `reason="建议您提供身高体重以获得精准推荐"`
2. 参考历史购买尺码记录提高准确性
3. 考虑商品的版型属性（修身/宽松）

---

## Tool 4: InventoryCheckTool

### Purpose
检查指定商品的库存和促销信息

### Java Signature

```java
@Tool("Check product inventory and promotion status")
public InventoryStatus checkInventory(
    @ToolParam(value = "productId", required = true) String productId,
    @ToolParam(value = "size", required = false) String size,
    @ToolParam(value = "color", required = false) String color
);
```

### Parameters

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `productId` | String | 是 | 商品ID |
| `size` | String | 否 | 指定尺码（若为空则返回所有尺码库存） |
| `color` | String | 否 | 指定颜色（若为空则返回所有颜色库存） |

### Return Type

```java
public class InventoryStatus {
    private String productId;
    private Integer totalStock;           // 总库存
    private Map<String, Integer> sizeStock; // {"S": 10, "M": 5, "L": 0}
    private String promotionInfo;         // "限时8折，还剩2天"
    private Boolean lowStockWarning;      // true if stock < 10
}
```

### Business Rules
1. 若库存低于 10 件，设置 `lowStockWarning=true`
2. 促销信息从营销系统实时获取
3. 库存数据缓存 5 分钟

---

## Tool 5: VisualSimilaritySearchTool

### Purpose
基于图片向量检索视觉相似或差异的商品（处理元数据缺失场景）

### Java Signature

```java
@Tool("Search products by visual similarity or dissimilarity")
public List<Product> searchByVisual(
    @ToolParam(value = "referenceProductId", required = false) String referenceProductId,
    @ToolParam(value = "referenceImageUrl", required = false) String referenceImageUrl,
    @ToolParam(value = "searchMode", required = true) String searchMode,
    @ToolParam(value = "limit", required = false, defaultValue = "10") Integer limit
);
```

### Parameters

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `referenceProductId` | String | 否 | 参考商品ID（与referenceImageUrl二选一） |
| `referenceImageUrl` | String | 否 | 用户上传的图片URL |
| `searchMode` | String | 是 | `SIMILAR`（找相似）或 `DISSIMILAR`（找差异） |
| `limit` | Integer | 否 | 返回数量 |

### Business Rules
1. `SIMILAR` 模式: 余弦相似度 > 0.8
2. `DISSIMILAR` 模式: 余弦相似度 < 0.5
3. 若用户上传图片，实时调用 CLIP 模型生成 Embedding

### Example Agent Usage

```text
User: "不喜欢这个领口，换个款式"

Agent思考:
- 用户拒绝商品 prod-123
- 元数据中无"领口"标签
- 使用视觉差异性检索

Agent调用:
searchByVisual(referenceProductId="prod-123", searchMode="DISSIMILAR", limit=5)

Agent回复:
"好的，我为您挑选了几款领口设计不同的款式：[商品列表]"
```

---

## Tool 6: UpdateUserPreferenceTool

### Purpose
更新用户画像中的偏好标签（基于对话推断）

### Java Signature

```java
@Tool("Update user preference based on conversation")
public void updateUserPreference(
    @ToolParam(value = "userId", required = true) String userId,
    @ToolParam(value = "stylePreferences", required = false) List<String> stylePreferences,
    @ToolParam(value = "colorPreferences", required = false) Map<String, Integer> colorPreferences,
    @ToolParam(value = "budgetRange", required = false) BudgetRange budgetRange
);
```

### Parameters

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `userId` | String | 是 | 用户ID |
| `stylePreferences` | List<String> | 否 | 新增的风格标签 |
| `colorPreferences` | Map<String, Integer> | 否 | 颜色偏好计数（增量） |
| `budgetRange` | BudgetRange | 否 | 预算范围 |

### Return Type
`void`

### Business Rules
1. 增量更新（不覆盖已有偏好）
2. 异步执行（不阻塞对话）
3. 每次对话结束时自动触发

---

## Tool 7: GetProductDetailTool

### Purpose
获取单个商品的完整详情（含材质、用户评价等）

### Java Signature

```java
@Tool("Get detailed information of a product")
public ProductDetail getProductDetail(
    @ToolParam(value = "productId", required = true) String productId
);
```

### Return Type

```java
public class ProductDetail extends Product {
    private String material;           // "桑蚕丝+聚酯纤维"
    private String sellingPoints;      // "丝绒面料显贵气，V领修饰颈部"
    private Double rating;             // 4.7
    private Integer reviewCount;       // 320
    private List<String> topReviews;   // 精选用户评价
}
```

---

## Error Handling Contract

所有工具在遇到错误时，应抛出以下标准异常：

### ToolExecutionException

```java
public class ToolExecutionException extends RuntimeException {
    private String errorCode;    // "PRODUCT_NOT_FOUND", "INVALID_PARAMETER"
    private String userMessage;  // 适合向用户展示的错误信息
}
```

### Error Codes

| 错误码 | 说明 | Agent应对策略 |
|--------|------|--------------|
| `PRODUCT_NOT_FOUND` | 商品不存在 | 告知用户该商品已下架，推荐其他款式 |
| `INVALID_PARAMETER` | 参数格式错误 | 重新解析用户输入 |
| `SERVICE_UNAVAILABLE` | 下游服务不可用 | 告知用户稍后重试 |
| `INSUFFICIENT_DATA` | 数据不足（如用户未提供身高体重） | 向用户追问缺失信息 |

---

## Testing Strategy

### Unit Testing
每个 Tool 的 Service 层独立测试：
```java
@Test
void testSearchProducts_withCategoryAndPrice() {
    List<Product> results = productRetrievalService.search("连衣裙", null, null, null, 500.0, null);
    assertThat(results).allMatch(p -> p.getCategory().equals("连衣裙") && p.getPrice() <= 500.0);
}
```

### Integration Testing (REST Assured)
使用 REST Assured 验证工具行为：
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductSearchToolIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Test
    void testSearchProducts() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(Map.of(
                "category", "连衣裙",
                "maxPrice", 1000.0
            ))
        .when()
            .post("/internal/tools/search-products")
        .then()
            .statusCode(200)
            .body("products.size()", greaterThan(0))
            .body("products[0].category", equalTo("连衣裙"))
            .body("products[0].price", lessThanOrEqualTo(1000.0f));
    }
}
```

---

## Version History

- **v1.0** (2026-01-06): 初始版本，定义7个核心工具
