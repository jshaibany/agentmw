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

@Configuration
@PropertySource({ "classpath:application.properties" })
@EnableJpaRepositories(
    basePackages = "com.onecashye.web.security.telepin.*", 
    entityManagerFactoryRef = "tpEntityManager", 
    transactionManagerRef = "tpTransactionManager"
)
public class PersistenceTelepinConfiguration {

	@Autowired
    private Environment env;
    
	@Bean(name = "telepinEM")
    public LocalContainerEntityManagerFactoryBean tpEntityManager() {
        LocalContainerEntityManagerFactoryBean em
          = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(tpDataSource());
        em.setPackagesToScan(
          new String[] { "com.onecashye.web.security.telepin.*" });

        HibernateJpaVendorAdapter vendorAdapter
          = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto",
          env.getProperty("spring.tp.jpa.hibernate.ddl-auto"));
        properties.put("hibernate.dialect",
          env.getProperty("spring.tp.jpa.database-platform"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public DataSource tpDataSource() {
 
        DriverManagerDataSource dataSource
          = new DriverManagerDataSource();
        dataSource.setDriverClassName(
          env.getProperty("spring.tp.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.tp.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.tp.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.tp.datasource.password"));

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager tpTransactionManager() {
 
        JpaTransactionManager transactionManager
          = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
          tpEntityManager().getObject());
        return transactionManager;
    }
}
