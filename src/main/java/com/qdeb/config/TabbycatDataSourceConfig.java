package com.qdeb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class TabbycatDataSourceConfig {

    @Bean(name = "tabbycatDataSource")
    @ConfigurationProperties(prefix = "tabbycat.datasource")
    public DataSource tabbycatDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "tabbycatJdbcTemplate")
    public JdbcTemplate tabbycatJdbcTemplate(@Qualifier("tabbycatDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
