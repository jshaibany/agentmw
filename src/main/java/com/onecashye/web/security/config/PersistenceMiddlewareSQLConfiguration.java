package com.onecashye.web.security.config;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@PropertySource({ "classpath:application.properties" })
@EnableJpaRepositories(
    basePackages = "com.onecashye.web.security.middleware.sql.*", 
    entityManagerFactoryRef = "mwSqlEntityManager", 
    transactionManagerRef = "mwSqlTransactionManager"
)
@EnableTransactionManagement
public class PersistenceMiddlewareSQLConfiguration {

	@Autowired
    private Environment env;
    
	
    @Bean(name = "middlewareSqlEM")
    public LocalContainerEntityManagerFactoryBean mwSqlEntityManager() {
        LocalContainerEntityManagerFactoryBean em
          = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(mwSqlDataSource());
        em.setPackagesToScan(
          new String[] { "com.onecashye.web.security.middleware.sql.*" });
        //em.setPersistenceUnitName("middlewareUnit");
        HibernateJpaVendorAdapter vendorAdapter
          = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.ddl-auto",
          env.getProperty("spring.mw.sql.jpa.hibernate.ddl-auto"));
        properties.put("hibernate.dialect",
          env.getProperty("spring.mw.sql.jpa.database-platform"));
        em.setJpaPropertyMap(properties);

        return em;
    }

	
    @Bean
    public DataSource mwSqlDataSource() {
 
        DriverManagerDataSource dataSource
          = new DriverManagerDataSource();
        dataSource.setDriverClassName(
          env.getProperty("spring.mw.sql.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.mw.sql.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.mw.sql.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.mw.sql.datasource.password"));

        return dataSource;
    }

	
    @Bean
    public PlatformTransactionManager mwSqlTransactionManager() {
 
        JpaTransactionManager transactionManager
          = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
          mwSqlEntityManager().getObject());
        return transactionManager;
    }
}
