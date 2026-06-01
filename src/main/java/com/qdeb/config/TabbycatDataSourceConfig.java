package com.qdeb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class TabbycatDataSourceConfig {

    @Primary
    @Bean(name = "dataSourceProperties")
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "dataSource")
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "tabbycatDataSource")
    public DataSource tabbycatDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl("jdbc:postgresql://172.18.0.3:5432/tabbycat");
        ds.setUsername("tabbycat");
        ds.setPassword("tabbycat");
        return ds;
    }

    @Bean(name = "tabbycatJdbcTemplate")
    public JdbcTemplate tabbycatJdbcTemplate(@Qualifier("tabbycatDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
