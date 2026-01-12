package com.weis.demo.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileDTO {
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 店铺ID
     */
    private Long storeId;
    
    /**
     * 会员昵称
     */
    private String nickname;
    
    /**
     * 会员等级
     */
    private Integer level;
    
    /**
     * 积分余额
     */
    private Integer points;
    
    /**
     * 会员状态 (1-正常, 2-冻结, 3-注销)
     */
    private Integer status;
    
    /**
     * 注册时间
     */
    private LocalDateTime registerTime;
    
    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;
    
    /**
     * 会员标签
     */
    private String tags;
    
    /**
     * 备注信息
     */
    private String remark;
}
