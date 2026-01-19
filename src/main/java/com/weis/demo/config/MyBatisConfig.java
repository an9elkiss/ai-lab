package com.weis.demo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 *
 * @author weis
 * @date 2025-01-16
 */
@Configuration
@MapperScan("com.weis.demo.mapper")
public class MyBatisConfig {
    // MyBatis 配置已通过 application.yml 完成
    // 如需自定义配置，可在此处添加
}
