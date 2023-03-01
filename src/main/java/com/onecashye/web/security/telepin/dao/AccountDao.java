package com.onecashye.web.security.telepin.dao;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

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
public class AccountDao {


	private final EntityManager entityManager;
	private final Environment env;
	
	@Autowired
	public AccountDao(@Qualifier("telepinEM") EntityManager entityManager, Environment env) {
		this.entityManager = entityManager;
		this.env = env;
		
	}
	
	@Transactional(value="tpTransactionManager",readOnly = true)
	public String getAccountUniversalID(String account_id){
		
		Object universal_id = entityManager
				.createNativeQuery("SELECT ACC_UNIVERSAL_ID FROM "+env.getProperty("view.accounts")+" " + 
						"WHERE ACC_ID=:account_id ")
				.setParameter("account_id", account_id)
				.getSingleResult();

		return universal_id.toString();
		
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountChildren(String parent_id){
		
		List<Object[]> children = entityManager
				.createNativeQuery("SELECT CHILD_ID, " +
						"CHILD_NAME, "+
						"CHILD_MOBILE, "+
						"CHILD_STATUS "+
						"FROM "+env.getProperty("view.account.childs")+" " + 
						"WHERE PARENT_ID=:parent_id ")
				.setParameter("parent_id", parent_id)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] child : children) {
			
			Map<String,Object> s = new HashMap<>();
			
		    s.put("account_id", child[0]);
		    s.put("account_name", child[1]);
		    s.put("account_mobile", child[2]);
		    s.put("account_status", child[3]);
		    		    
		    result.add(s);
		    
		}
		
		return result;
		
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountChildrenForCashupReport(String parent_id){
		
		List<Object[]> children = entityManager
				.createNativeQuery("SELECT CHILD_ID, " +
						"CHILD_NAME, "+
						"CHILD_MOBILE, "+
						"CHILD_STATUS "+
						"FROM "+env.getProperty("view.account.childs")+" " + 
						"WHERE PARENT_ID=:parent_id ")
				.setParameter("parent_id", parent_id)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] child : children) {
			
			Map<String,Object> s = new HashMap<>();
			
		    s.put("Id", child[0]);
		    s.put("Title", child[1]);
		    s.put("account_mobile", child[2]);
		    s.put("account_status", child[3]);
		    		    
		    result.add(s);
		    
		}
		
		return result;
		
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountNominatedDetails(String account_id){
		
		List<Object[]> records = entityManager
				.createNativeQuery("SELECT  " + 
						"   C.NOMINATED_ACC_ID, "+
						"   C.CUSTOMER_ID, "+
						"   C.MOBILE_NUMBER,  " + 
						"   C.NOMINEE_TYPE, "+
						"   C.NOMINEE_NAME, "+
						"   C.BRAND_ID,  " + 
						"   C.NOMINEE_STATUS, "+
						"   C.NOMINEE_CUST_ID, "+
						"   C.NOMINEE_MOBILE_NUMBER,  " + 
						"   C.BANK_ACCOUNT_ID, "+
						"   C.NOMINEE_CURRENCY "+
						"FROM "+env.getProperty("view.nominated.account")+" C "+
						"WHERE C.CUSTOMER_ID = :account_id "+
						"AND C.NOMINEE_STATUS=1 ")
				.setParameter("account_id", account_id)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] r : records) {
			
			Map<String,Object> s = new HashMap<>();
			
		    s.put("nominated_account_id", r[0]);
		    s.put("customer_id", r[1]);
		    s.put("mobile_number", r[2]);
		    s.put("nominee_type", r[3]);
		    s.put("nominee_name", r[4]);
		    s.put("brand_id", r[5]);
		    s.put("nominee_status", r[6]);
		    s.put("nominee_cust_id", r[7]);
		    s.put("nominee_mobile_number", r[8]);
		    s.put("bank_account_id", r[9]);
		    s.put("nominee_currency", r[10]);
		    
		    		    
		    result.add(s);
		    
		}
		
		return result;
		
	}
		
}
