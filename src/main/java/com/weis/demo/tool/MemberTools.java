package com.weis.demo.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MemberTools {

    @Tool(name = "member-coupon-query", value = "查询用户的可用优惠券列表，包括优惠券类型、面额、使用条件和有效期等信息")
    public List<Map<String, Object>> queryMemberCoupons(@ToolMemoryId String memoryId) {
        log.info("查询用户优惠券，会员ID: {}", memoryId);
        
        // 模拟用户优惠券数据
        return List.of(
            Map.of(
                "couponId", "CPN001",
                "couponName", "新用户专享券",
                "couponType", "满减券",
                "discountAmount", new BigDecimal("50.00"),
                "minOrderAmount", new BigDecimal("299.00"),
                "validFrom", "2024-12-01 00:00:00",
                "validTo", "2025-01-31 23:59:59",
                "status", "可使用",
                "description", "满299元减50元，仅限新用户使用"
            ),
            Map.of(
                "couponId", "CPN002", 
                "couponName", "品类专享券",
                "couponType", "折扣券",
                "discountRate", "0.85",
                "minOrderAmount", new BigDecimal("199.00"),
                "validFrom", "2024-12-20 00:00:00",
                "validTo", "2025-02-28 23:59:59",
                "status", "可使用",
                "description", "服装类商品满199元享8.5折优惠"
            ),
            Map.of(
                "couponId", "CPN003",
                "couponName", "生日专享券", 
                "couponType", "满减券",
                "discountAmount", new BigDecimal("100.00"),
                "minOrderAmount", new BigDecimal("599.00"),
                "validFrom", "2024-12-25 00:00:00",
                "validTo", "2025-01-25 23:59:59",
                "status", "可使用",
                "description", "生日月专享，满599元减100元"
            )
        );
    }
}
