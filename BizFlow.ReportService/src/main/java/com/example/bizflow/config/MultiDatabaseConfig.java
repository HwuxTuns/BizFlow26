package com.example.bizflow.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.bizflow.repository", entityManagerFactoryRef = "multiEntityManager", transactionManagerRef = "multiTransactionManager")
public class MultiDatabaseConfig {

    @Value("${DB_HOST:mysql}")
    private String dbHost;

    @Value("${DB_PASSWORD:123456}")
    private String dbPassword;

    @Primary
    @Bean(name = "authDataSource")
    public DataSource authDataSource() {
        return DataSourceBuilder
                .create()
                .url("jdbc:mysql://" + dbHost
                        + ":3306/bizflow_auth_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8")
                .username("root")
                .password(dbPassword)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Bean(name = "catalogDataSource")
    public DataSource catalogDataSource() {
        return DataSourceBuilder
                .create()
                .url("jdbc:mysql://" + dbHost
                        + ":3306/bizflow_catalog_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8")
                .username("root")
                .password(dbPassword)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Bean(name = "customerDataSource")
    public DataSource customerDataSource() {
        return DataSourceBuilder
                .create()
                .url("jdbc:mysql://" + dbHost
                        + ":3306/bizflow_customer_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8")
                .username("root")
                .password(dbPassword)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Primary
    @Bean(name = "multiEntityManager")
    public LocalContainerEntityManagerFactoryBean multiEntityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("authDataSource") DataSource authDataSource,
            @Qualifier("catalogDataSource") DataSource catalogDataSource,
            @Qualifier("customerDataSource") DataSource customerDataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");

        // Cấu hình routing datasource
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("auth", authDataSource);
        targetDataSources.put("catalog", catalogDataSource);
        targetDataSources.put("customer", customerDataSource);

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(authDataSource);
        routingDataSource.afterPropertiesSet();

        return builder
                .dataSource(routingDataSource)
                .packages("com.example.bizflow.entity")
                .persistenceUnit("multi")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "multiTransactionManager")
    public PlatformTransactionManager multiTransactionManager(
            @Qualifier("multiEntityManager") LocalContainerEntityManagerFactoryBean multiEntityManager) {
        return new JpaTransactionManager(multiEntityManager.getObject());
    }
}
