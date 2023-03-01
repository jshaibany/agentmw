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
    basePackages = "com.onecashye.web.security.middleware.*", 
    entityManagerFactoryRef = "mwEntityManager", 
    transactionManagerRef = "mwTransactionManager"
)
@EnableTransactionManagement
public class PersistenceMiddlewareConfiguration {

	@Autowired
    private Environment env;
    
	
    @Bean(name = "middlewareEM")
    public LocalContainerEntityManagerFactoryBean mwEntityManager() {
        LocalContainerEntityManagerFactoryBean em
          = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(mwDataSource());
        em.setPackagesToScan(
          new String[] { "com.onecashye.web.security.middleware.*" });
        //em.setPersistenceUnitName("middlewareUnit");
        HibernateJpaVendorAdapter vendorAdapter
          = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.ddl-auto",
          env.getProperty("spring.mw.jpa.hibernate.ddl-auto"));
        properties.put("hibernate.dialect",
          env.getProperty("spring.mw.jpa.database-platform"));
        em.setJpaPropertyMap(properties);

        return em;
    }

	
    @Bean
    public DataSource mwDataSource() {
 
        DriverManagerDataSource dataSource
          = new DriverManagerDataSource();
        dataSource.setDriverClassName(
          env.getProperty("spring.mw.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.mw.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.mw.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.mw.datasource.password"));

        return dataSource;
    }

	
    @Bean
    public PlatformTransactionManager mwTransactionManager() {
 
        JpaTransactionManager transactionManager
          = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
          mwEntityManager().getObject());
        return transactionManager;
    }
}
