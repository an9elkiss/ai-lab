package com.weis.demo.tool;

import cn.hutool.json.JSONUtil;
import com.weis.demo.dto.ItemDTO;
import com.weis.demo.dto.command.ItemSearchCmd;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ItemTools {

    private final RestTemplate restTemplate;

    public ItemTools() {
        this.restTemplate = new RestTemplate();
    }

    @Tool(name = "item-search-tool", value = """
            帮助用户找到合适的商品，返回匹配的商品列表。
            调用此工具后将意图设置为item_search，并将返回的数据填入\\"searchResult\\"字段。
            完全信任item-search-tool返回的数据，即使返回了空列表后者看似无关的商品。
            如果返回了空列表，代表没有合适的商品。根据这一事实结合你的角色定位生成合理的回复。
            
            **注意**：
            仅当用户输入中明确包含或可清晰推断出以下全部两个关键属性时，才能使用item-search-tool：
            1.目标用户性别：商品主要穿着者的性别。
            2.穿着场合/场景：商品计划被使用的具体场合（如：上班、约会、婚礼、度假、日常通勤）。
            处理逻辑：
            如果以上任一属性缺失或模糊，智能体应优先判定为 other意图，并通过主动追问进行澄清，不得直接使用item-search-tool。
            """)
    public List<ItemDTO> search(@P("""
            JSON格式的搜索参数，形如：
            {
              "keyWord": "主要品类关键词，如'连衣裙'",
              "scene": "场景",
              "gender": "性别（male/female）",
              "style": "风格",
              "color": "颜色",
              "priceRange": "价格区间"
            }
            尽可能从用户输入中提取并填充上述字段。
            """) String params) {
        try {

            ItemSearchCmd cmd = JSONUtil.toBean(params, ItemSearchCmd.class);

            // 构建请求URL
            String baseUrl = "https://weisapi.baozun.com/api/v2/item/search";
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("pageNo", 1)
                    .queryParam("pageSize", 3)
                    .queryParam("keyword", cmd.getKeyWord());

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("content-type", "application/json");
            headers.set("x-ma-c", "84a7e4fb8abda8843afb8f51919d0ebe");
            headers.set("x-shop-c", "polenebs");

            // 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 发送GET请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            // 处理响应数据
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // 检查响应状态
                String statusCode = (String) responseBody.get("statusCode");
                if (!"100010".equals(statusCode)) {
                    log.error("API返回错误状态: {}, 消息: {}", statusCode, responseBody.get("msg"));
                    return List.of();
                }
                
                // 获取data对象
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                
                if (data != null) {
                    // 获取items数组
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
                    
                    if (items != null) {
                        return items.stream()
                                .map(this::mapToItemDTO)
                                .toList();
                    }
                }
            }

            return List.of(); // 返回空列表
        } catch (Exception e) {
            // 记录错误日志
            log.error("搜索商品时发生错误: {}", e.getMessage(), e);
            return List.of(); // 返回空列表
        }
    }

    private ItemDTO mapToItemDTO(Map<String, Object> itemMap) {
        // 根据实际API响应结构和ItemDTO映射数据
        ItemDTO item = new ItemDTO();
        
        // 映射标题字段 - API中直接有title字段
        if (itemMap.get("title") != null) {
            item.setTitle(itemMap.get("title").toString());
        }
        
        // 映射价格字段 - API中直接有salePrice字段
        if (itemMap.get("salePrice") != null) {
            try {
                // API返回的是Double类型，直接转换
                Object priceObj = itemMap.get("salePrice");
                if (priceObj instanceof Number) {
                    item.setSalePrice(((Number) priceObj).doubleValue());
                } else {
                    item.setSalePrice(Double.valueOf(priceObj.toString()));
                }
            } catch (NumberFormatException e) {
                log.warn("价格转换失败: {}", itemMap.get("salePrice"));
                item.setSalePrice(0.0);
            }
        }
        
        return item;
    }
}
