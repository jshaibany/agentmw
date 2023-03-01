package com.onecashye.web.security.middleware.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class BillerDao {


	private final EntityManager entityManager;
	
	
	@Autowired
	public BillerDao(@Qualifier("middlewareEM") EntityManager entityManager) {
		
		this.entityManager=entityManager;
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> getBillerDefinition(String gateway){
		
		/*
		 * get Biller definition
		 */
		
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM web_biller_definition AS WBD " + 
				 "WHERE WBD.GATEWAY = :gateway " + 
				 "AND WBD.STATUS = 'A' ")
				.setParameter("gateway", gateway)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Biller_Id",(Integer) request[0]);
			r.put("Gateway",(String) request[1]);
			r.put("Provider_Name",(String) request[2]);
			r.put("Provider_Code",(String) request[3]);
			r.put("Type",(String) request[4]);
			r.put("Has_Topup",(Boolean) request[5]);
			r.put("Has_Bundles",(Boolean) request[6]);
			r.put("Allow_User_Amount",(Boolean) request[7]);
			r.put("UOM",(String) request[8]);
			r.put("Multi_Factor",(BigDecimal) request[9]);
			
			if(request[10] != null)
			r.put("Brand",(Integer) request[10]);
			
			//r.put("Status",(Character) request[11]);
			
			if(request[12]!=null)
				r.put("Minimum_Amount",(BigDecimal) request[12]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> getBillerDefinition(String gateway,String code,String type){
		
		/*
		 * get Biller definition
		 */
		
		if(type == null) {
			
			//For other billers than Telecom & MNOs
			return getBillerDefinition(gateway, code);
		}
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM web_biller_definition AS WBD " + 
				 "WHERE WBD.GATEWAY = :gateway " + 
				 "AND WBD.PROVIDER_CODE = :code " +
				 "AND WBD.TYPE = :type " +
				 "AND WBD.STATUS = 'A' ")
				.setParameter("gateway", gateway)
				.setParameter("code", code)
				.setParameter("type", type)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Biller_Id",(Integer) request[0]);
			r.put("Gateway",(String) request[1]);
			r.put("Provider_Name",(String) request[2]);
			r.put("Provider_Code",(String) request[3]);
			r.put("Type",(String) request[4]);
			r.put("Has_Topup",(Boolean) request[5]);
			r.put("Has_Bundles",(Boolean) request[6]);
			r.put("Allow_User_Amount",(Boolean) request[7]);
			r.put("UOM",(String) request[8]);
			r.put("Multi_Factor",(BigDecimal) request[9]);
			
			if(request[10] != null)
			r.put("Brand",(Integer) request[10]);
			
			//r.put("Status",(Character) request[11]);
			
			if(request[12]!=null)
				r.put("Minimum_Amount",(BigDecimal) request[12]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> getBillerDetails(Integer parent){
		
		/*
		 * get Biller details
		 */
		
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM web_biller_details AS WBD " + 
				 "WHERE WBD.PARENT_ID = :parent " + 
				 "AND WBD.STATUS = 'A' ")
				.setParameter("parent", parent)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Id",(Integer) request[0]);
			r.put("Parent_Id",(Integer) request[1]);
			r.put("Type",(String) request[2]);
			r.put("Name_Ar",(String) request[3]);
			r.put("Name_En",(String) request[4]);
			r.put("Cost",(BigDecimal) request[5]);
			r.put("Currency",(String) request[6]);
			r.put("Brand",(Integer) request[7]);
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}

	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> getBillerDetails(Integer parent,String type){
		
		/*
		 * get Biller details
		 */
		
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM web_biller_details AS WBD " + 
				 "WHERE WBD.PARENT_ID = :parent " + 
				 "AND WBD.TYPE = :type " + 
				 "AND WBD.STATUS = 'A' ")
				.setParameter("parent", parent)
				.setParameter("type", type)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Id",(Integer) request[0]);
			r.put("Parent_Id",(Integer) request[1]);
			r.put("Type",(String) request[2]);
			r.put("Name_Ar",(String) request[3]);
			r.put("Name_En",(String) request[4]);
			r.put("Cost",(BigDecimal) request[5]);
			r.put("Currency",(String) request[6]);
			r.put("Brand",(Integer) request[7]);
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}

	@SuppressWarnings("unchecked")
	private List<Map<String,Object>> getBillerDefinition(String gateway,String code){
		
		/*
		 * get Biller definition
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM web_biller_definition AS WBD " + 
				 "WHERE WBD.GATEWAY = :gateway " + 
				 "AND WBD.PROVIDER_CODE = :code " +
				 "AND WBD.TYPE IS NULL " +
				 "AND WBD.STATUS = 'A' ")
				.setParameter("gateway", gateway)
				.setParameter("code", code)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Biller_Id",(Integer) request[0]);
			r.put("Gateway",(String) request[1]);
			r.put("Provider_Name",(String) request[2]);
			r.put("Provider_Code",(String) request[3]);
			r.put("Type",null);
			r.put("Has_Topup",(Boolean) request[5]);
			r.put("Has_Bundles",(Boolean) request[6]);
			r.put("Allow_User_Amount",(Boolean) request[7]);
			r.put("UOM",(String) request[8]);
			r.put("Multi_Factor",(BigDecimal) request[9]);
			
			if(request[10] != null)
			r.put("Brand",(Integer) request[10]);
			
			//r.put("Status",(Character) request[11]);
			
			if(request[12]!=null)
				r.put("Minimum_Amount",(BigDecimal) request[12]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
}
