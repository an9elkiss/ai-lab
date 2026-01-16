package com.weis.demo.dto;

import lombok.Data;

@Data
public class AIMessageDTO {

    private String userInput;
    private String emotion;
    private String intentType;
    private String keyInfo;
    private AIMessageKeyDTO keyInfoDTO;

}
