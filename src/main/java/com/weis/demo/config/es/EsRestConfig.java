/**
 * Copyright (c) 2017 Baozun All Rights Reserved.
 * <p>
 * This software is the confidential and proprietary information of Baozun. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license agreement you entered into with Baozun.
 * <p>
 * BAOZUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. BAOZUN SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.weis.demo.config.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class EsRestConfig {

    @Autowired
    private Elasticsearch8Config elasticsearch8Config;

    @Bean
    public RestClient restClient() {
        List<HttpHost> httpHosts = new ArrayList<>();

        Assert.notNull(elasticsearch8Config.getNodeAddress8(), "nodeAddress8 defined");
        String[] nodes = elasticsearch8Config.getNodeAddress8().split(",");

        for (String node : nodes) {
            try {
                String[] parts = node.split(":");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");

                httpHosts.add(new HttpHost(parts[0], Integer.parseInt(parts[1]), elasticsearch8Config.getProtocol8()));
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid ES8 nodes " + "property '" + node + "'", ex);
            }
        }

        // 使用Es8ClientBuilder构建客户端
        Es8ClientBuilder clientBuilder = Es8ClientBuilder.build(httpHosts);

        // 设置认证信息
        if (StringUtils.isNotBlank(elasticsearch8Config.getUserName8())
                && StringUtils.isNotBlank(elasticsearch8Config.getPassword8())) {

            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials usernamePasswordCredentials =
                    new UsernamePasswordCredentials(elasticsearch8Config.getUserName8(), elasticsearch8Config.getPassword8());
            credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials);

            clientBuilder.setCredentialsProvider(credentialsProvider);
        }

        return clientBuilder.create();
    }

    /**
     * 创建ElasticsearchClient Bean，使用Elasticsearch8Config配置连接到不同的ES实例
     */
    @Bean
    public ElasticsearchClient getElasticsearchClient(RestClient restClient) {

         // fix - 字段从有值变为无值时，无法清除ES中该字段的值
         ObjectMapper mapper = new ObjectMapper();
         mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
         JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(mapper);

         // 创建Transport和ElasticsearchClient
         RestClientTransport transport = new RestClientTransport(restClient, jsonpMapper);
         return new ElasticsearchClient(transport);
    }

}
