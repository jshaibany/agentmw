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
public class SalesOrderDao {

Logger logger = LogManager.getLogger(SalesOrderDao.class);
	
	private final EntityManager entityManager;
	private final Environment env;
	
	
	@Autowired
	public SalesOrderDao(@Qualifier("telepinEM") EntityManager entityManager, Environment env) {
		
		this.entityManager=entityManager;
		this.env = env;
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findSalesOrder(String order_id, String account_msisdn){
		
		/*
		 * 31052022
		 * 
		 * Get Sales Orders by SO# & Account MSISDN of the requested user
		 * 
		 * And where status is POSTED to avoid retrieving other types of transactions
		 */
		List<Object[]> requests = entityManager.createNativeQuery(
				"SELECT sales_order_number," + 
						"sales_order_date," + 
						"source_msisdn," + 
						"source_account_id," + 
						"destination_msisdn," + 
						"extra_info1," + 
						"extra_info2," + 
						"extra_info3," + 
						"extra_info4," + 
						"\"ORIGINAL AMOUNT\"," + 
						"service_name," + 
						"\"TOTAL AMOUNT\"," + 
						"transaction_status," + 
						"currency," + 
						"brand_name," + 
						"fees "+ 	 
				"FROM "+env.getProperty("view.salesorder.info2")+" SOI " + 
				"WHERE SOI.SALES_ORDER_NUMBER =:order_id "+
				"AND SOI.DESTINATION_MSISDN =:account_msisdn "+
				"AND SOI.TRANSACTION_STATUS ='POSTED' ")
				.setParameter("order_id", order_id)
				.setParameter("account_msisdn", account_msisdn)
				.getResultList();
	
		
		List<Map<String,String>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,String> r = new HashMap<>();
			
			r.put("Order_Id",String.format("%s", request[0]));
			r.put("Order_Date",String.format("%s", request[1]));
			r.put("Source_MSISDN",String.format("%s", request[2]));
			r.put("Source_ID",String.format("%s", request[3]));
			r.put("Destination_MSISDN",String.format("%s", request[4]));
			r.put("Extra_Info1",String.format("%s", request[5]));
			r.put("Extra_Info2",String.format("%s", request[6]));
			r.put("Extra_Info3",String.format("%s", request[7]));
			r.put("Extra_Info4",String.format("%s", request[8]));
			r.put("Original_Amount",String.format("%s", request[9]));
			r.put("Service_Name",String.format("%s", request[10]));
			r.put("Total_Amount",String.format("%s", request[11]));
			r.put("Status",String.format("%s", request[12]));
			r.put("Currency",String.format("%s", request[13]));
			r.put("Brand_Name",String.format("%s", request[14]));
			r.put("Fees",String.format("%s", request[15]));
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findCashOutSalesOrder(String order_id, String account_msisdn){
		
		/*
		 * 31082022
		 * 
		 * Note: to be used for CashOut trx ONLY
		 * 
		 * Get Sales Orders by SO# & Account MSISDN of the requested user as destination account
		 * 
		 * And where status is POSTED to avoid retrieving other types of transactions
		 */
		List<Object[]> requests = entityManager.createNativeQuery(
				"SELECT SALES_ORDER_NUMBER, "+ 
				"SALES_ORDER_DATE, "+
				"SOURCE_MSISDN, "+
				"SOURCE_ACCOUNT_ID, "+
				"DESTINATION_MSISDN, "+
				"EXTRA_INFO1, "+
				"EXTRA_INFO2, "+
				"EXTRA_INFO3, "+
				"EXTRA_INFO4, "+
				" \"ORIGINAL AMOUNT\", "+
				"SERVICE_NAME, "+
				" \"TOTAL AMOUNT\", "+
				"TRANSACTION_STATUS, "+
				"CURRENCY, "+
				"BRAND_NAME, "+
				"FEES, "+
				"REMARK "+
				"FROM "+env.getProperty("view.salesorder.info2")+" SOI " + 
				"WHERE SOI.SALES_ORDER_NUMBER =:order_id "+
				"AND SOI.DESTINATION_MSISDN =:account_msisdn "+
				"AND SOI.BRAND_NAME LIKE '"+env.getProperty("cashout.brand.name")+"' "+
				"AND SOI.TRANSACTION_STATUS ='POSTED' ")
				.setParameter("order_id", order_id)
				.setParameter("account_msisdn", account_msisdn)
				.getResultList();
	
		
		List<Map<String,String>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,String> r = new HashMap<>();
			
			r.put("Order_Id",String.format("%s", request[0]));
			r.put("Order_Date",String.format("%s", request[1]));
			r.put("Source_MSISDN",String.format("%s", request[2]));
			r.put("Source_ID",String.format("%s", request[3]));
			r.put("Destination_MSISDN",String.format("%s", request[4]));
			r.put("Extra_Info1",String.format("%s", request[5]));
			r.put("Extra_Info2",String.format("%s", request[6]));
			r.put("Extra_Info3",String.format("%s", request[7]));
			r.put("Extra_Info4",String.format("%s", request[8]));
			r.put("Original_Amount",String.format("%s", request[9]));
			r.put("Service_Name",String.format("%s", request[10]));
			r.put("Total_Amount",String.format("%s", request[11]));
			r.put("Status",String.format("%s", request[12]));
			r.put("Currency",String.format("%s", request[13]));
			r.put("Brand_Name",String.format("%s", request[14]));
			r.put("Fees",String.format("%s", request[15]));
			r.put("Remark",String.format("%s", request[16]));
			
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findSalesOrderRefundTrx(String order_id, String account_msisdn){
		
		
		List<Object[]> requests = entityManager.createNativeQuery(
				"SELECT sales_order_number," + 
				"sales_order_date," + 
				"source_msisdn," + 
				"source_account_id," + 
				"destination_msisdn," + 
				"extra_info1," + 
				"extra_info2," + 
				"extra_info3," + 
				"extra_info4," + 
				"\"ORIGINAL AMOUNT\"," + 
				"service_name," + 
				"\"TOTAL AMOUNT\"," + 
				"transaction_status," + 
				"currency," + 
				"brand_name," + 
				"fees "+ 	
						"FROM "+env.getProperty("view.salesorder.info2")+" SOI " + 
				 		"WHERE SOI.ORIG_SALES_ORDER =:order_id "+
				 		"AND SOI.DESTINATION_MSISDN =:account_msisdn "+
				 		"AND SOI.TRANSACTION_STATUS ='REFUND' ")
				.setParameter("order_id", order_id)
				.setParameter("account_msisdn", account_msisdn)
				.getResultList();
		
		List<Map<String,String>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,String> r = new HashMap<>();
			
			r.put("Order_Id",String.format("%s", request[0]));
			r.put("Order_Date",String.format("%s", request[1]));
			r.put("Source_MSISDN",String.format("%s", request[2]));
			r.put("Source_ID",String.format("%s", request[3]));
			r.put("Destination_MSISDN",String.format("%s", request[4]));
			r.put("Extra_Info1",String.format("%s", request[5]));
			r.put("Extra_Info2",String.format("%s", request[6]));
			r.put("Extra_Info3",String.format("%s", request[7]));
			r.put("Extra_Info4",String.format("%s", request[8]));
			r.put("Original_Amount",String.format("%s", request[9]));
			r.put("Service_Name",String.format("%s", request[10]));
			r.put("Total_Amount",String.format("%s", request[11]));
			r.put("Status",String.format("%s", request[12]));
			r.put("Currency",String.format("%s", request[13]));
			r.put("Brand_Name",String.format("%s", request[14]));
			r.put("Fees",String.format("%s", request[15]));
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}

	
}
