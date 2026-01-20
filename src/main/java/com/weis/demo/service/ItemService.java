package com.weis.demo.service;

import com.weis.demo.dto.ItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ItemService {

    private final RestTemplate restTemplate;

    public ItemService() {
        this.restTemplate = new RestTemplate();
    }

    public List<ItemDTO> search(String keyWord) {
        try {
            // 构建请求URL
            String baseUrl = "https://weisapi.baozun.com/api/v2/item/search?keyword="+keyWord+"&pageNo=1&pageSize=3";
//            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl)
//                    .queryParam("pageNo", 1)
//                    .queryParam("pageSize", 3)
//                    .queryParam("keyword", cmd.getKeyWord());

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("content-type", "application/json");
            headers.set("x-ma-c", "84a7e4fb8abda8843afb8f51919d0ebe");
            headers.set("x-shop-c", "polenebs");

            // 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 发送GET请求
            ResponseEntity<Map> response = restTemplate.exchange(
//                    uriBuilder.toUriString(),
                    baseUrl,
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
