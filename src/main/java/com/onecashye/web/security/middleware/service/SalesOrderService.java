package com.onecashye.web.security.middleware.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.onecashye.web.security.middleware.dao.SalesOrderDao;

@Service
public class SalesOrderService {

	Logger logger = LogManager.getLogger(SalesOrderService.class);
	
	private final SalesOrderDao salesOrderDao;
	
	@Autowired
	public SalesOrderService(SalesOrderDao salesOrderDao) {
		
		this.salesOrderDao = salesOrderDao;
		
	}

	public Optional<List<Map<String,String>>> findSalesOrder(String order_id, String account_msisdn){
		
		List<Map<String,String>> r = salesOrderDao.findSalesOrder(order_id,account_msisdn);
		
		if(r.size()<=0)
			return Optional.ofNullable(null);
		
		return Optional.ofNullable(r);
	}
	
	public Optional<Map<String,String>> findCashOutSalesOrder(String order_id, String account_msisdn){
		
		/*
		 * 31082022
		 * 
		 * To get CashOut Sales Orders and parse delegate data
		 */
		List<Map<String,String>> r = salesOrderDao.findCashOutSalesOrder(order_id,account_msisdn);
		
		if(r.size()<=0)
			return Optional.ofNullable(null);
		
		Map<String,String> result = new HashMap<>(r.get(0));
		
		String delegate_data = "";
		
		/*
		 * CashOut Orders done by Web Sales Requests, the delegate data should be in Remark column
		 */
		if(!(result.get("Remark") == null) || !(result.get("Remark").isEmpty())) {
			
			delegate_data = result.get("Remark");
			
			logger.info(String.format("Delegate data is found %s", delegate_data));
		}
			
		
		/*
		 * CashOut Orders done by App Single Shot PAYMENT API, the delegate data should be in Extra Info2 column
		 */
		if(!(result.get("Extra_Info2") == null) || !(result.get("Extra_Info2").isEmpty())) {
			
			delegate_data = result.get("Extra_Info2");
			
			logger.info(String.format("Delegate data is found %s", delegate_data));
		}
			
		
		try {
			
			String[] delegate_details = delegate_data.split("#");
			
			result.put("Auth_Name", delegate_details[0]);
			result.put("Auth_ID", delegate_details[1]);
			result.put("Auth_Mobile", delegate_details[2]);
		}
		catch(Exception e) {
			
			logger.error(String.format("Error while parsing delegate data", e.getMessage()));
		}
		
		return Optional.ofNullable(result);
	}
	
	public Optional<List<Map<String,String>>> findSalesOrderRefundTrx(String order_id, String account_msisdn){
		
		List<Map<String,String>> r = salesOrderDao.findSalesOrderRefundTrx(order_id,account_msisdn);
		
		if(r.size()<=0)
			return Optional.ofNullable(null);
		
		return Optional.ofNullable(r);
	}
}
