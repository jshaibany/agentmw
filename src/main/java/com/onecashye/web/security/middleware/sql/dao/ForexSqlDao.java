package com.onecashye.web.security.middleware.sql.dao;

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
import org.springframework.transaction.annotation.Transactional;

@Repository
@PropertySources({
    @PropertySource("classpath:dao.properties"),
    @PropertySource("classpath:tcs.properties")
})
public class ForexSqlDao {

	Logger logger = LogManager.getLogger(ForexSqlDao.class);
	
	private final EntityManager entityManager;
	private final Environment env;
	
	@Autowired
	public ForexSqlDao(@Qualifier("middlewareSqlEM") EntityManager entityManager, Environment env) {
		
		this.entityManager=entityManager;
		this.env = env;
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer insertNewForex(Map<String,Object> entity) {
		
		//Added 02102022
		
				try {
					
					int c =entityManager.createNativeQuery(
						    "INSERT INTO "+env.getProperty("table.forex.rates")+" (" + 
						    "	USD," + 
						    "	SAR," + 
						    "	CBY," + 
						    "	CreatedBy" +
						    "	) VALUES (?,?,?,?)" )
							.setParameter(1, entity.get(env.getProperty("USD")))
							.setParameter(2, entity.get(env.getProperty("SAR")))
							.setParameter(3, entity.get(env.getProperty("CBY_USD")))
							.setParameter(4, entity.get("Created_By"))
							.executeUpdate();
					
					if(c <= 0)
					 return null;
					
					
					return c;
				}
				catch(Exception e) {
					
					e.printStackTrace();
					
					return null;
				}
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> getLatestForexRate(){
		
		/*
		 * 02102022
		 * 
		 * 
		 */
		
		List<Object[]> rates = entityManager.createNativeQuery(
				 "SELECT USD, "+ 
				 "SAR, "+
				 "CBY "+
				 "FROM "+env.getProperty("table.forex.rates")+" " + 
				 "WHERE ID=(SELECT MAX(ID) FROM "+env.getProperty("table.forex.rates")+" " +") "+
				 "")
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(rates.size()<=0)
			return null;
		
		for(Object[] rate : rates) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put(env.getProperty("USD"),rate[0]);
			r.put(env.getProperty("SAR"),rate[1]);
			r.put(env.getProperty("CBY_USD"),rate[2]);
		
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
}
