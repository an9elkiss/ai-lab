package com.weis.demo.tool;

import com.weis.demo.dto.ItemDTO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.output.structured.Description;
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

    @Tool(name = "item-search-tool", value = "根据关键词搜索商品信息，返回匹配的商品列表。可以搜索商品名称、颜色、类别等相关信息，帮助用户找到合适的商品。")
    public List<ItemDTO> search(@P("搜索关键词，可以是商品名称、颜色、类别等，例如：连衣裙、太阳镜、风衣") String keyword) {
        try {
            // 构建请求URL
            String baseUrl = "https://weisapi-sit.baozun.com/api/v2/item/search";
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("pageNo", 1)
                    .queryParam("pageSize", 3)
                    .queryParam("keyword", keyword);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("content-type", "application/json");
            headers.set("x-ma-c", "E95A68EC92C5F7BED265CC899514E591");
            headers.set("x-shop-c", "test");

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
