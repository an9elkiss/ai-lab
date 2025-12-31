package com.weis.demo.config.es;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
public class Elasticsearch8Config {

    @Value("${cluster.name8}")
    private String clusterName8;

    @Value("${node.address8}")
    private String nodeAddress8;

    @Value("${es.username8}")
    private String userName8;

    @Value("${es.password8}")
    private String password8;

    @Value("${es.protocol8:http}")
    private String protocol8;
}
