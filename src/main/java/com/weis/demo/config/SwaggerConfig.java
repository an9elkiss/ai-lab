package com.weis.demo.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 基于SpringDoc OpenAPI 3实现
 *
 * @author weis
 * @date 2025-12-16
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("微商城API接口文档")
                        .description("基于Spring Boot 3.5.6 + SpringDoc OpenAPI的接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("微商城团队")
                                .email("support@weis.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目文档")
                        .url("https://docs.weis.com"));
    }

}

