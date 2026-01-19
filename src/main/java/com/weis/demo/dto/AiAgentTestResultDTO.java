package com.weis.demo.dto;

import com.weis.demo.entity.AiAgentTestCase;
import com.weis.demo.entity.AiAgentTestResult;
import lombok.Data;

@Data
public class AiAgentTestResultDTO extends AiAgentTestResult {

    private AiAgentTestCase aiAgentTestCase;
}
