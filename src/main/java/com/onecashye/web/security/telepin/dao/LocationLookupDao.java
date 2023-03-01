package com.onecashye.web.security.telepin.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LocationLookupDao {

	private final EntityManager entityManager;
	
	@Autowired
	public LocationLookupDao(@Qualifier("telepinEM") EntityManager entityManager) {
		
		this.entityManager=entityManager;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getAllProvinces(){
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT LOCATION_ID,LOCATION_NAME,ARABIC_NAME "+ 
				 "FROM V$LOCATIONS LOC " + 
				 "WHERE LOC.LOCATION_TYPE = 'PVN'  " + 
				 "AND LOC.PARENT_LOC_ID = 254 "+
				 "ORDER BY ARABIC_NAME ASC ")
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			//r.put("Location_Id",(BigDecimal) request[0]);
			//r.put("Name_EN",(String) request[1]);
			//r.put("Name_AR",(String) request[2]);
			
			r.put("Location_Id",request[0]);
			r.put("Name_EN",request[1]);
			r.put("Name_AR",request[2]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
		
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getCities(String province){
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT LOCATION_ID,LOCATION_NAME,ARABIC_NAME "+ 
				 "FROM V$LOCATIONS LOC " + 
				 "WHERE LOC.LOCATION_TYPE = 'CTY'  " + 
				 "AND LOC.PARENT_LOC_ID = :province "+
				 "ORDER BY ARABIC_NAME ASC ")
				.setParameter("province", province)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Location_Id",request[0]);
			r.put("Name_EN",request[1]);
			r.put("Name_AR",request[2]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
		
	}
}
