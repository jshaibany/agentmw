package com.onecashye.web.security.middleware.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;


@Repository
@PropertySources({
    @PropertySource("classpath:dao.properties"),
    @PropertySource("classpath:tcs.properties")
})
public class BrandLookupDao {

	Logger logger = LogManager.getLogger(BrandLookupDao.class);
	
	private final EntityManager entityManager;
	private final Environment env;
	
	@Autowired
	public BrandLookupDao(@Qualifier("middlewareEM") EntityManager entityManager, Environment env) {
		
		this.entityManager=entityManager;
		this.env = env;
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> getBrand(String trx_type,String currency){
		
		/*
		 * get Brand
		 */
		
		
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM "+env.getProperty("table.lookup.brands")+" AS WBL " + 
				 "WHERE WBL.TRANSACTION_TYPE = :trx_type "+
				 "AND WBL.CURRENCY = :currency ")
				.setParameter("trx_type", trx_type)
				.setParameter("currency", currency)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		logger.info(String.format("Query for trx_type=%s and currency=%s is %s row(s)", trx_type,
				currency,
				requests.size()));
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			
			r.put("Brand",(Integer) request[2]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}

	public List<Map<String,Object>> getBrand(String trx_type){
		
		/*
		 * get Brand from properties file to replace DB table
		 */
		
		String b = env.getProperty(trx_type);
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		Map<String,Object> r = new HashMap<>();
		
		r.put("Brand",Integer.parseInt(b));
		
		result.add(r);
		
		return result;

	}
}
