package com.weis.demo.service;

import com.weis.demo.dto.MemberProfileDTO;
import com.weis.demo.dto.command.MemberProfileFindCmd;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MemberProfileService {

    // 模拟数据存储
    private static final Map<String, MemberProfileDTO> MOCK_DATA = new HashMap<>();
    
    static {
        // 初始化一些模拟数据
        MOCK_DATA.put("1:1001", MemberProfileDTO.builder()
            .memberId(1001L)
            .storeId(1L)
            .nickname("张三")
            .level(3)
            .points(1500)
            .status(1)
            .registerTime(LocalDateTime.of(2023, 6, 15, 10, 30))
            .lastActiveTime(LocalDateTime.now().minusDays(2))
            .tags("VIP,活跃用户")
            .remark("优质客户")
            .build());
            
        MOCK_DATA.put("1:1002", MemberProfileDTO.builder()
            .memberId(1002L)
            .storeId(1L)
            .nickname("李四")
            .level(2)
            .points(800)
            .status(1)
            .registerTime(LocalDateTime.of(2023, 8, 20, 14, 15))
            .lastActiveTime(LocalDateTime.now().minusDays(5))
            .tags("新用户")
            .remark("潜力客户")
            .build());
            
        MOCK_DATA.put("2:2001", MemberProfileDTO.builder()
            .memberId(2001L)
            .storeId(2L)
            .nickname("王五")
            .level(4)
            .points(2300)
            .status(1)
            .registerTime(LocalDateTime.of(2023, 3, 10, 9, 45))
            .lastActiveTime(LocalDateTime.now().minusDays(1))
            .tags("VIP,高消费")
            .remark("重要客户")
            .build());
    }

    /**
     * 根据条件查找会员档案
     * 
     * @param cmd 查找命令
     * @return 会员档案信息
     */
    public MemberProfileDTO find(MemberProfileFindCmd cmd) {
        log.info("查找会员档案，storeId: {}, memberId: {}", cmd.getStoreId(), cmd.getMemberId());
        
        // 参数验证
        if (cmd.getStoreId() == null) {
            throw new IllegalArgumentException("店铺ID不能为空");
        }
        if (cmd.getMemberId() == null) {
            throw new IllegalArgumentException("会员ID不能为空");
        }
        
        // 构造查找键
        String key = cmd.getStoreId() + ":" + cmd.getMemberId();
        
        // 从模拟数据中查找
        MemberProfileDTO profile = MOCK_DATA.get(key);
        
        if (profile != null) {
            log.info("找到会员档案: {}", profile.getNickname());
            return profile;
        }
        
        // 如果没有找到，返回一个默认的档案信息
        log.info("未找到会员档案，返回默认信息");
        return MemberProfileDTO.builder()
            .memberId(cmd.getMemberId())
            .storeId(cmd.getStoreId())
            .nickname("新用户" + cmd.getMemberId())
            .level(1)
            .points(0)
            .status(1)
            .registerTime(LocalDateTime.now())
            .lastActiveTime(LocalDateTime.now())
            .tags("新注册")
            .remark("系统默认档案")
            .build();
    }
}
