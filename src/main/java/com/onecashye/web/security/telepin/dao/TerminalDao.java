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
public class TerminalDao {

    private final EntityManager entityManager;
    private final Environment env;
	
	@Autowired
	public TerminalDao(@Qualifier("telepinEM") EntityManager entityManager, Environment env) {
		this.entityManager = entityManager;
		this.env = env;
		
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getTerminalDetails(String username,String account_id){
		
		//Changes: 24042022
		//1- Add one column to query USER_ALIAS
		//2- (String) terminal[n].toString() to be (String) terminal[n] to avoid null exceptions
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT USER_ID,USER_MSISDN,USER_ALIAS,USER_EMAIL FROM "+env.getProperty("view.account.users.all")+" " + 
						"WHERE USER_ACCOUNT_ID=:account_id " + 
						"AND DATE_DISABLED IS NULL "+
						"AND USER_NAME=:username ")
				.setParameter("username", username)
				.setParameter("account_id", account_id)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
				if(terminal[0]!=null)
					s.put("user_id", terminal[0]);
				else
					s.put("user_id", "");
			    
					//s.put("mobile_number", (String) terminal[1]);
			    	//s.put("alias_number", (String) terminal[2]);
			    	//s.put("user_email", (String) terminal[3]);
			    
			    	s.put("mobile_number", terminal[1]);
			    	s.put("alias_number", terminal[2]);
			    	s.put("user_email", terminal[3]);
			    		    
			    	result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getUserNotificationNumber(String username){
		
		//Changes: 24042022
		//1- (String) terminal[n].toString() to be (String) terminal[n] to avoid null exceptions
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT USER_NAME,MOBILE_NUMBER FROM "+env.getProperty("view.users.all")+" " + 
						"WHERE USER_NAME=:username ")
				.setParameter("username", username)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
			    //s.put("user_name", (String) terminal[0]);
			    //s.put("mobile_number", (String) terminal[1]);
			    
			    s.put("user_name", terminal[0]);
			    s.put("mobile_number", terminal[1]);
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountTerminals(String account_id,String terminal_type){
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT ATU.* " + 
						"FROM "+env.getProperty("view.account.terminal.users")+" ATU, " + 
						""+env.getProperty("view.users.all")+" AUS " + 
						"WHERE ATU.USER_KEY = AUS.USER_KEY " + 
						"AND AUS.ACCESS_TYPE=:terminal_type " + 
						"AND ATU.CUSTOMER_ID=:account_id ")
				.setParameter("terminal_type", terminal_type)
				.setParameter("account_id", account_id)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
				s.put("user_name", terminal[0]);
			    s.put("user_id", terminal[1]);
			    s.put("user_key", terminal[2]);
			    s.put("user_mobile", terminal[3]);
			    s.put("user_email", terminal[4]);
			    s.put("customer_id", terminal[5]);
			    s.put("staff_name", terminal[6]);
			    s.put("last_name", terminal[7]);
			    s.put("lang", terminal[8]);
			    if(terminal[9]==null)
			    	s.put("status", "A");
			    else
			    	s.put("status", terminal[9]);
			    s.put("account_mobile", terminal[11]);
			    s.put("role_id", terminal[12]);
			    s.put("role_name", terminal[13]);
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountTerminals(String username, String account_id,String terminal_type){
		
		//Overload added 25042022 to filter Terminals and exclude the requster and similar role
		//Note:
		//		This condition (AND ATU.ROLE_NAME NOT LIKE '%Master%') is useless if role name is edited
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT ATU.* " + 
						"FROM "+env.getProperty("view.account.terminal.users")+" ATU, " + 
						""+env.getProperty("view.users.all")+" AUS " + 
						"WHERE ATU.USER_KEY = AUS.USER_KEY " + 
						"AND AUS.ACCESS_TYPE=:terminal_type " + 
						"AND ATU.ROLE_ID > :role_id " + 
						//"AND ATU.ROLE_NAME NOT LIKE '%Master%' " +
						"AND ATU.CUSTOMER_ID=:account_id ")
				.setParameter("terminal_type", terminal_type)
				.setParameter("role_id", getUserRole(username))
				.setParameter("account_id", account_id)
				.getResultList();
		
		String lang = getUserLanguage(username);

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
				s.put("user_name", terminal[0]);
			    s.put("user_id", terminal[1]);
			    s.put("user_key", terminal[2]);
			    s.put("user_mobile", terminal[3]);
			    s.put("user_email", terminal[4]);
			    s.put("customer_id", terminal[5]);
			    s.put("staff_name", terminal[6]);
			    s.put("last_name", terminal[7]);
			    s.put("lang", terminal[8]);
			    if(terminal[9]==null)
			    	s.put("status", "A");
			    else
			    	s.put("status", terminal[9]);
			    s.put("account_mobile", terminal[11]);
			    s.put("role_id", terminal[12]);
			    
			    if(lang.contains("Arabic")) {
			    	
			    	s.put("role_name", terminal[14]);
			    }
			    else {
			    	
			    	s.put("role_name", terminal[13]);
			    }
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountTerminals(List<String> privileges,String username, String account_id,String terminal_type){
		
		//Edit 27042022
		//Changed the view and all previous logic
		
		//Overload added 25042022 to filter Terminals and exclude the requster and similar role
		//Note:
		//		This condition (AND ATU.ROLE_NAME NOT LIKE '%Master%') is useless if role name is edited
		
		/*
		 * Modified 16052022
		 * 
		 * To add both WEB & GPT listing using two different quiries
		 * 
		 * Modified 21052022
		 * 
		 * "AND ATU.DATE_SUSPENDED IS NULL "+
		 */
		
		
		if(terminal_type.contentEquals("WEB")) {
			
			List<Object[]> terminals = entityManager
					.createNativeQuery("SELECT user_name," + 
							"customer_id," + 
							"user_key," + 
							"role_id," + 
							"mobile_number," + 
							"status," + 
							"user_id," + 
							"staff_name," + 
							"email," + 
							"last_name," + 
							"role_name," + 
							"role_desc " + 
							"FROM "+env.getProperty("view.web.access.roles")+"  " + 
							"WHERE ACCESS_TYPE=:terminal_type " + 
							"AND PRIVILEGE_NAME IN (:privileges) " +
							"AND DATE_SUSPENDED IS NULL "+
							"AND CUSTOMER_ID=:account_id ")
					.setParameter("terminal_type", terminal_type)
					.setParameter("privileges", privileges)
					.setParameter("account_id", account_id)
					.getResultList();
			
			String lang = getUserLanguage(username);

			List<Map<String,Object>> result = new ArrayList<>();
			
				for(Object[] terminal : terminals) {
					
					Map<String,Object> s = new HashMap<>();
					
					s.put("user_name", terminal[0]);
					s.put("customer_id", terminal[1]);
					s.put("user_key", terminal[2]);
					s.put("role_id", terminal[3]);
					
					s.put("user_mobile", terminal[4]);
					if(terminal[5]==null)
				    	s.put("status", "A");
				    else
				    	s.put("status", terminal[5]);
				    s.put("user_id", terminal[6]);
				    s.put("staff_name", terminal[7]);
				    s.put("user_email", terminal[8]);
				    s.put("last_name", terminal[9]);
				    
				    s.put("lang", lang);
				    s.put("account_mobile", terminal[4]);
				    
				    
				    if(lang.contains("Arabic")) {
				    	
				    	s.put("role_name", terminal[11]);
				    }
				    else {
				    	
				    	s.put("role_name", terminal[10]);
				    }
				    		    
				    result.add(s);
				    
				}
				
			return result;
		}
		else {
			
			List<Object[]> terminals = entityManager
					.createNativeQuery("SELECT user_name," + 
							"customer_id," + 
							"user_key," + 
							"role_id," + 
							"mobile_number," + 
							"status," + 
							"user_id," + 
							"staff_name," + 
							"email," + 
							"last_name," + 
							"role_name," + 
							"role_desc " + 
							"FROM "+env.getProperty("view.app.access.roles")+" " + 
							"WHERE ACCESS_TYPE=:terminal_type " + 
							"AND PRIVILEGE_NAME IN (:privileges) " +
							"AND DATE_SUSPENDED IS NULL "+
							"AND CUSTOMER_ID=:account_id ")
					.setParameter("terminal_type", terminal_type)
					.setParameter("privileges", privileges)
					.setParameter("account_id", account_id)
					.getResultList();
			
			String lang = getUserLanguage(username);

			List<Map<String,Object>> result = new ArrayList<>();
			
				for(Object[] terminal : terminals) {
					
					Map<String,Object> s = new HashMap<>();
					
					s.put("user_name", terminal[0]);
					s.put("customer_id", terminal[1]);
					s.put("user_key", terminal[2]);
					s.put("role_id", terminal[3]);
					
					s.put("user_mobile", terminal[4]);
					if(terminal[5]==null)
				    	s.put("status", "A");
				    else
				    	s.put("status", terminal[5]);
				    s.put("user_id", terminal[6]);
				    s.put("staff_name", terminal[7]);
				    s.put("user_email", terminal[8]);
				    s.put("last_name", terminal[9]);
				    
				    s.put("lang", lang);
				    s.put("account_mobile", terminal[4]);
				    
				    
				    if(lang.contains("Arabic")) {
				    	
				    	s.put("role_name", terminal[11]);
				    }
				    else {
				    	
				    	s.put("role_name", terminal[10]);
				    }
				    		    
				    result.add(s);
				    
				}
				
			return result;
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountWebTerminals4Cashup(List<String> privileges,String username, String account_id,String terminal_type){
		
		/*
		 * Added 13052022
		 * 
		 * For Non-Edgecom Web
		 */
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT USER_NAME, " + 
						"CUSTOMER_ID,"+
						"USER_KEY "+
						"FROM "+env.getProperty("view.web.access.roles")+" ATU " + 
						"WHERE ATU.ACCESS_TYPE=:terminal_type " + 
						"AND (ATU.PRIVILEGE_NAME IN (:privileges) OR ATU.USER_NAME= :username ) " +
						"AND DATE_SUSPENDED IS NULL "+
						"AND ATU.CUSTOMER_ID=:account_id ")
				.setParameter("terminal_type", terminal_type)
				.setParameter("privileges", privileges)
				.setParameter("username", username)
				.setParameter("account_id", account_id)
				.getResultList();
		

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
				s.put("user_name", terminal[0]);
				s.put("customer_id", terminal[1]);
				s.put("user_key", terminal[2]);
				
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountAppTerminals4Cashup(List<String> privileges,String username, String account_id,String terminal_type){
		
		/*
		 * Added 13052022
		 * 
		 * For Non-Edgecom Web
		 * 
		 * It is for Cashup report droplist filtering
		 */
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT USER_NAME, " +
						"CUSTOMER_ID,"+
						"USER_KEY, "+
						"USER_ID "+
						"FROM "+env.getProperty("view.app.access.roles")+" ATU " + 
						"WHERE ATU.ACCESS_TYPE=:terminal_type " + 
						"AND ATU.PRIVILEGE_NAME IN (:privileges)  " +
						"AND DATE_SUSPENDED IS NULL "+
						"AND ATU.CUSTOMER_ID=:account_id ")
				.setParameter("terminal_type", terminal_type)
				.setParameter("privileges", privileges)
				.setParameter("account_id", account_id)
				.getResultList();
		

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
				s.put("user_name", terminal[0]);
				s.put("customer_id", terminal[1]);
				s.put("user_key", terminal[2]);
				s.put("staff_code", terminal[3]);
				
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountTerminals(String account_id){
		
		//Added for Edgecom only (Cashup)
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT ATU.* " + 
						"FROM "+env.getProperty("view.account.terminal.users")+" ATU, " + 
						""+env.getProperty("view.users.all")+" AUS " + 
						"WHERE ATU.USER_KEY = AUS.USER_KEY " + 
						"AND ATU.USER_ID='000' " + 
						"AND ATU.CUSTOMER_ID=:account_id ")
				.setParameter("account_id", account_id)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
				s.put("Title", terminal[0]);
			    s.put("user_id", terminal[1]);
			    s.put("Id", terminal[2]);
			    s.put("user_mobile", terminal[3]);
			    s.put("user_email", terminal[4]);
			    s.put("customer_id", terminal[5]);
			    s.put("staff_name", terminal[6]);
			    s.put("last_name", terminal[7]);
			    s.put("lang", terminal[8]);
			    if(terminal[9]==null)
			    	s.put("status", "A");
			    else
			    	s.put("status", terminal[9]);
			    s.put("account_mobile", terminal[11]);
			    s.put("role_id", terminal[12]);
			    s.put("role_name", terminal[13]);
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getSelfTerminal(String account_id,String user_name){
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT ATU.* " + 
						"FROM "+env.getProperty("view.account.terminal.users")+" ATU, " + 
						""+env.getProperty("view.users.all")+" AUS " + 
						"WHERE ATU.USER_KEY = AUS.USER_KEY " + 
						"AND AUS.USER_NAME=:user_name " + 
						"AND ATU.CUSTOMER_ID=:account_id ")
				.setParameter("user_name", user_name)
				.setParameter("account_id", account_id)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
				s.put("Title", terminal[0]);
			    s.put("user_id", terminal[1]);
			    s.put("Id", terminal[2]);
			    s.put("user_mobile", terminal[3]);
			    s.put("user_email", terminal[4]);
			    s.put("customer_id", terminal[5]);
			    s.put("staff_name", terminal[6]);
			    s.put("last_name", terminal[7]);
			    s.put("lang", terminal[8]);
			    if(terminal[9]==null)
			    	s.put("status", "A");
			    else
			    	s.put("status", terminal[9]);
			    s.put("account_mobile", terminal[11]);
			    s.put("role_id", terminal[12]);
			    s.put("role_name", terminal[13]);
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAccountGptStaff(String account_id){
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT ATU.* " + 
						"FROM "+env.getProperty("view.account.terminal.users")+" ATU, " + 
						""+env.getProperty("view.users.all")+" AUS " + 
						"WHERE ATU.USER_KEY = AUS.USER_KEY " + 
						"AND ATU.USER_ID <> '000' " + 
						"AND AUS.ACCESS_TYPE='GPT' " + 
						"AND ATU.CUSTOMER_ID=:account_id ")
				.setParameter("account_id", account_id)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
				s.put("Title", String.format("%s - %s", terminal[0],terminal[1]));
			    s.put("user_id", terminal[1]);
			    s.put("Id", terminal[2]);
			    s.put("user_mobile", terminal[3]);
			    s.put("user_email", terminal[4]);
			    s.put("customer_id", terminal[5]);
			    s.put("staff_name", terminal[6]);
			    s.put("last_name", terminal[7]);
			    s.put("lang", terminal[8]);
			    if(terminal[9]==null)
			    	s.put("status", "A");
			    else
			    	s.put("status", terminal[9]);
			    s.put("account_mobile", terminal[11]);
			    s.put("role_id", terminal[12]);
			    s.put("role_name", terminal[13]);
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@Transactional(value="tpTransactionManager",readOnly = true)
	public String countActiveTerminal(String username){
		
		Object terminals = entityManager
				.createNativeQuery("SELECT COUNT(*) FROM "+env.getProperty("view.account.users.all")+" " + 
						"WHERE USER_NAME=:username " + 
						"AND DATE_DISABLED IS NULL ")
				.setParameter("username", username)
				.getSingleResult();


		try {
			
			return terminals.toString();
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return "-1";
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> getTerminalTypesLookup(String account_id,String terminal_type){
		
		//TODO Deprecate
		//To get Terminal Types for Lookup dropdown lists
		//Terminal Type = WEB or GPT
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT BANK_USER_ACCESS_ID,ACCESS_ROLE_ID,ROLE_NAME,ROLE_DESC " +
						"FROM "+env.getProperty("view.bank.users")+" " + 
						"WHERE ACCOUNT_ID=:account_id " + 
						"AND TERMINAL_TYPE_DESC= :terminal_type ")
				.setParameter("account_id", account_id)
				.setParameter("terminal_type", terminal_type)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
			    s.put("role_id", terminal[0]);
			    s.put("access_id", terminal[1]);
			    s.put("role_name", (String) terminal[2]);
			    s.put("role_desc", (String) terminal[3]);
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> getTerminalTypesLookup(String username, String account_id,String terminal_type){
		
		//TODO Deprecate
		//Overload added 25042022 to filter the requester and similar role
		// Note:
		//		This works if the requester role is unique to the level of the GROUP
		
		//To get Terminal Types for Lookup dropdown lists
		//Terminal Type = WEB or GPT
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT DISTINCT BANK_USER_ACCESS_ID,ACCESS_ROLE_ID,ROLE_NAME,ROLE_DESC " +
						"FROM "+env.getProperty("view.bank.users")+" UAI, "+env.getProperty("view.account.users.all")+" AUS " + 
						"WHERE UAI.ACCOUNT_ID = AUS.USER_ACCOUNT_ID " + 
						"AND ACCOUNT_ID=:account_id " + 
						"AND ACCESS_ROLE_ID > :role_id "+
						"AND TERMINAL_TYPE_DESC= :terminal_type ")
				.setParameter("account_id", account_id)
				.setParameter("terminal_type", terminal_type)
				.setParameter("role_id", getUserRole(username))
				.getResultList();

		String lang = getUserLanguage(username);
		
		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
			    s.put("role_id", terminal[0]);
			    s.put("access_id", terminal[1]);
			    
			    if(lang.contains("Arabic")) {
			    	
			    	s.put("role_name", terminal[3]);
				    s.put("role_desc", terminal[3]);
			    }
			    else {
			    	
			    	s.put("role_name", terminal[2]);
				    s.put("role_desc", terminal[2]);
			    }
			    
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	/*
	 * Create 16052022 4 functions
	 * 
	 * getWebTerminalTypesLookup4Master
	 * getGptTerminalTypesLookup4Master
	 * getWebTerminalTypesLookup4Manager
	 * getGptTerminalTypesLookup4Manager
	 * 
	 * To get role names for drop lists
	 * 
	 * Each function only does a single task and gets a single terminal type
	 */
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> getWebTerminalTypesLookup4Master(String username, String account_id){
		
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT DISTINCT BANK_USER_ACCESS_ID,ACCESS_ROLE_ID,ROLE_NAME,ROLE_DESC " +
						"FROM "+env.getProperty("view.bank.users")+" UAI " + 
						"WHERE ACCOUNT_ID=:account_id " + 
						"AND TERMINAL_TYPE_DESC='WEB' ")
				.setParameter("account_id", account_id)
				.getResultList();

		String lang = getUserLanguage(username);
		
		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
			    s.put("role_id", terminal[0]);
			    s.put("access_id", terminal[1]);
			    
			    if(lang.contains("Arabic")) {
			    	
			    	s.put("role_name", terminal[3]);
				    s.put("role_desc", terminal[3]);
			    }
			    else {
			    	
			    	s.put("role_name", terminal[2]);
				    s.put("role_desc", terminal[2]);
			    }
			    
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> getGptTerminalTypesLookup4Master(String username, String account_id){
		
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT DISTINCT BANK_USER_ACCESS_ID,ACCESS_ROLE_ID,ROLE_NAME,ROLE_DESC " +
						"FROM "+env.getProperty("view.bank.users")+" UAI " + 
						"WHERE ACCOUNT_ID=:account_id " + 
						"AND TERMINAL_TYPE_DESC='GPT' ")
				.setParameter("account_id", account_id)
				.getResultList();

		String lang = getUserLanguage(username);
		
		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
			    s.put("role_id", terminal[0]);
			    s.put("access_id", terminal[1]);
			    
			    if(lang.contains("Arabic")) {
			    	
			    	s.put("role_name", terminal[3]);
				    s.put("role_desc", terminal[3]);
			    }
			    else {
			    	
			    	s.put("role_name", terminal[2]);
				    s.put("role_desc", terminal[2]);
			    }
			    
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> getWebTerminalTypesLookup4Manager(String username, String account_id){
		
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT DISTINCT BANK_USER_ACCESS_ID,ACCESS_ROLE_ID,ROLE_NAME,ROLE_DESC " +
						"FROM "+env.getProperty("view.bank.users")+" UAI " + 
						"WHERE ACCOUNT_ID=:account_id " + 
						"AND ROLE_NAME NOT LIKE '%Manager Web' "+
						"AND TERMINAL_TYPE_DESC='WEB' ")
				.setParameter("account_id", account_id)
				.getResultList();

		String lang = getUserLanguage(username);
		
		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
			    s.put("role_id", terminal[0]);
			    s.put("access_id", terminal[1]);
			    
			    if(lang.contains("Arabic")) {
			    	
			    	s.put("role_name", terminal[3]);
				    s.put("role_desc", terminal[3]);
			    }
			    else {
			    	
			    	s.put("role_name", terminal[2]);
				    s.put("role_desc", terminal[2]);
			    }
			    
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> getGptTerminalTypesLookup4Manager(String username, String account_id){
		
		
		List<Object[]> terminals = entityManager
				.createNativeQuery("SELECT DISTINCT BANK_USER_ACCESS_ID,ACCESS_ROLE_ID,ROLE_NAME,ROLE_DESC " +
						"FROM "+env.getProperty("view.bank.users")+" UAI " + 
						"WHERE ACCOUNT_ID=:account_id " + 
						"AND ROLE_NAME NOT LIKE '%Manager GPT' "+
						"AND TERMINAL_TYPE_DESC='GPT' ")
				.setParameter("account_id", account_id)
				.getResultList();

		String lang = getUserLanguage(username);
		
		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] terminal : terminals) {
				
				Map<String,Object> s = new HashMap<>();
				
			    s.put("role_id", terminal[0]);
			    s.put("access_id", terminal[1]);
			    
			    if(lang.contains("Arabic")) {
			    	
			    	s.put("role_name", terminal[3]);
				    s.put("role_desc", terminal[3]);
			    }
			    else {
			    	
			    	s.put("role_name", terminal[2]);
				    s.put("role_desc", terminal[2]);
			    }
			    
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
	
	@Transactional(value="tpTransactionManager",readOnly = true)
	private String getUserRole(String username){
		
		Object role = entityManager
				.createNativeQuery("SELECT USER_ACCESS_ROLE_ID FROM "+env.getProperty("view.account.users.all")+" " + 
						"WHERE USER_NAME=:username AND DATE_DISABLED IS NULL ")
				.setParameter("username", username)
				.getSingleResult();


		try {
			
			return role.toString();
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return "-1";
		}
	}
	
	@Transactional(value="tpTransactionManager",readOnly = true)
	private String getUserLanguage(String username){
		
		Object lang = entityManager
				.createNativeQuery("SELECT USER_LANGUAGE FROM "+env.getProperty("view.account.users.all")+" " + 
						"WHERE USER_NAME=:username AND DATE_DISABLED IS NULL ")
				.setParameter("username", username)
				.getSingleResult();


		try {
			
			return lang.toString();
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return "Arabic";
		}
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<String> getUserMsisdnByAlias(String alias){
		
		List<String> terminals = entityManager
				.createNativeQuery("SELECT USER_ACCOUNT_MSISDN FROM "+env.getProperty("view.account.users.all")+" " + 
						"WHERE USER_ALIAS=:alias ")
				.setParameter("alias", alias)
				.getResultList();

		List<String> result = new ArrayList<>();
		
			for(String terminal : terminals) {
			    		    
				result.add(String.format("%s", terminal));
			    
			}
			
		return result;
	}
	
	
	
}


