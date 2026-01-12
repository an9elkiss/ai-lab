package com.weis.demo.dto.command;

import lombok.Data;

@Data
public class MemberProfileFindCmd {
    private Long storeId;
    private Long memberId;
}
