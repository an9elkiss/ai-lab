/**
 * Copyright (c) 2017 Baozun All Rights Reserved.
 * <p>
 * This software is the confidential and proprietary information of Baozun.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Baozun.
 * <p>
 * BAOZUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. BAOZUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.weis.demo.config.es;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.util.List;

/**
 * Elasticsearch 8.x 客户端构建器
 *
 * @author system
 * @date 2025/09/23
 * @since JDK 1.8
 **/
public class Es8ClientBuilder {

    private int connectTimeoutMillis = 3000;
    private int socketTimeoutMillis = 30000;
    private int connectionRequestTimeoutMillis = 3000;
    private int maxConnectPerRoute = 1000;
    private int maxConnectTotal = 1000;

    private CredentialsProvider credentialsProvider;

    private final List<HttpHost> httpHosts;

    private Es8ClientBuilder(List<HttpHost> httpHosts) {
        this.httpHosts = httpHosts;
    }

    public Es8ClientBuilder setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    public Es8ClientBuilder setSocketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
        return this;
    }

    public Es8ClientBuilder setConnectionRequestTimeoutMillis(int connectionRequestTimeoutMillis) {
        this.connectionRequestTimeoutMillis = connectionRequestTimeoutMillis;
        return this;
    }

    public Es8ClientBuilder setMaxConnectPerRoute(int maxConnectPerRoute) {
        this.maxConnectPerRoute = maxConnectPerRoute;
        return this;
    }

    public Es8ClientBuilder setMaxConnectTotal(int maxConnectTotal) {
        this.maxConnectTotal = maxConnectTotal;
        return this;
    }

    public Es8ClientBuilder setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public static Es8ClientBuilder build(List<HttpHost> httpHosts) {
        return new Es8ClientBuilder(httpHosts);
    }

    /**
     * 创建 ElasticsearchClient 实例
     *
     * @return ElasticsearchClient
     */
    public RestClient create() {
        HttpHost[] httpHostArr = httpHosts.toArray(new HttpHost[0]);
        RestClientBuilder builder = RestClient.builder(httpHostArr);

        // 设置请求配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(connectTimeoutMillis);
            requestConfigBuilder.setSocketTimeout(socketTimeoutMillis);
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeoutMillis);
            return requestConfigBuilder;
        });

        // 设置HTTP客户端配置
        if (credentialsProvider != null) {
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                httpClientBuilder.setMaxConnTotal(maxConnectTotal);
                httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                httpClientBuilder.setKeepAliveStrategy((httpResponse, httpContext) -> 60000L);
                return httpClientBuilder;
            });
        } else {
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setMaxConnTotal(maxConnectTotal);
                httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                httpClientBuilder.setKeepAliveStrategy((httpResponse, httpContext) -> 60000L);
                return httpClientBuilder;
            });
        }

       return builder.build();
    }

}
