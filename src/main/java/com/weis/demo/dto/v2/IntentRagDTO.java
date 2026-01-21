package com.weis.demo.dto.v2;

import lombok.Data;


@Data
public class IntentRagDTO {

    private String userInput;

    private String imageContent;

    private String embeddingQuery;

}
