package com.weis.demo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * MemoryId 信息 DTO
 */
@Data
public class MemoryIdInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3544753431317374408L;

    private String memoryId;
    private int memoryType = 1;
    private Long storeId;
    private Long shopId;
    private Long memberId;
    private String agentName;
}