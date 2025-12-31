package com.weis.demo.config.es;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
public class Elasticsearch8Config {

    @Value("${es8.cluster-name}")
    private String clusterName8;

    @Value("${es8.node-address}")
    private String nodeAddress8;

    @Value("${es8.username}")
    private String userName8;

    @Value("${es8.password}")
    private String password8;

    @Value("${es8.protocol}")
    private String protocol8;
}
