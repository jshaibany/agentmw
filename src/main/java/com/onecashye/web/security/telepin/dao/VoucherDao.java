package com.onecashye.web.security.telepin.dao;

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
public class VoucherDao {


	Logger logger = LogManager.getLogger(VoucherDao.class);
	
	private final EntityManager entityManager;
	private final Environment env;
	
	//Added this DAO 23-03-2022

	@Autowired
	public VoucherDao(@Qualifier("telepinEM") EntityManager entityManager, Environment env) {
		this.entityManager = entityManager;
		this.env = env;
		
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(value="tpTransactionManager",readOnly = true)
	public List<Map<String,Object>> getVoucherDetails(String sales_order){
		
		/*
		 * To get voucher data for redeem by agent
		 */
		List<Object[]> vouchers = entityManager
				.createNativeQuery("SELECT MSISDN,EXTRA_INFO2,DEST_ACTUAL_AMOUNT,EXTERNAL_MEMO FROM "+env.getProperty("view.salesorder.info1")+" " + 
						" WHERE SALES_ORDER_NUMBER=:order_number ")
				.setParameter("order_number", sales_order)
				.getResultList();

		List<Map<String,Object>> result = new ArrayList<>();
		
			for(Object[] voucher : vouchers) {
				
				Map<String,Object> s = new HashMap<>();
				
			    s.put("receiver_msisdn", String.format("%s", voucher[0]));
			    //Edit 13042022
			    
			    if(voucher[1]!=null && !voucher[1].toString().isEmpty()) {
			    	
			    	logger.info(String.format("Name Off Net %s", voucher[1]));
			    	s.put("receiver_name", String.format("%s", voucher[1]));
			    }
			    else {
			    	
			    	if(voucher[3]!=null && !voucher[3].toString().isEmpty()) {
				    	
				    	logger.info(String.format("Name Off Net %s", voucher[3]));
				    	s.put("receiver_name", String.format("%s", voucher[3]));
				    }
			    }
			    
			    if(s.get("receiver_name") == null || s.get("receiver_name").toString().isEmpty()) {
			    	
			    	logger.error("No Off Net Name Found !");
			    	s.put("receiver_name","لايوجد");
			    	
			    }
			    /////////////////////////////////////////////////////////
			    /*
			     * 2592022
			     * 
			     * Added split functionality to parse the receiver name for cases like sending vouchers in bulk
			     */
			    
			    String st = String.format("%s", s.get("receiver_name"));
			    String[] st_list=st.split("#");
			    
			    if(st.length()>2) {
			    	
			    	logger.warn(String.format("There is an error in provided data [%s]", st));
			    	logger.warn(String.format("The original data without split to be returned to end user .."));
			    	s.put("receiver_name",st);
			    }
			    else {
			    	
			    	//Always get the first value in array
			    	s.put("receiver_name",st_list[0]);
			    }
			    
			    
			    
			    
			    ////////////////////////////////////////////////////////////
			    
			    s.put("amount", String.format("%s", voucher[2]));
			    		    
			    result.add(s);
			    
			}
			
		return result;
	}
}
