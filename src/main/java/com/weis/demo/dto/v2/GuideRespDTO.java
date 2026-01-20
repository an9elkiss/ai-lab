package com.weis.demo.dto.v2;

import lombok.Data;

import java.util.List;

@Data
public class GuideRespDTO {

    /**
     * AI答复
     */
    private String reply;

    /**
     * 预制回答
     */
    private List<String> redefinedReplies;

}
