package com.onecashye.web.security.telepin.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
public class SalesRequestDao {

	Logger logger = LogManager.getLogger(SalesRequestDao.class);
	
	private final EntityManager entityManager;
	private final Environment env;
	
	
	@Autowired
	public SalesRequestDao(@Qualifier("telepinEM") EntityManager entityManager, Environment env) {
		
		this.entityManager=entityManager;
		this.env = env;
	}
	
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findSelfPendingSalesRequests(String username,
			String source_id,
			Integer pageNumber,
			Integer pageSize,
			String dest,
			LocalDateTime from,
			LocalDateTime to,
			String status,
			String currency){
		
		/*
		 * Find maker/checker requests of self
		 */
		
		/*
		 * Updated 07062022 for multi checker feature
		 * 
		 * Added condition: AND ((MCR.CHECKED_STATUS ='RQS' AND MCR.LAST_ACTION IS NULL) OR (MCR.CHECKED_STATUS <> 'RQS' AND MCR.LAST_ACTION='REJECT') OR (MCR.CHECKED_STATUS = 'Checked2' AND MCR.LAST_ACTION='APPROVE'))
		 */
		
		//Remove Privilege Condition
		//Added "AND MCR.BULK_FLAG='N' " On 10042022
		//Added extractCashoutPendingRequest() function
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT REQUEST_ID, "+ 
				 "SERVICE_CODE, "+
				 "ORIGINAL_AMOUNT, "+
				 "PAYABLE_AMOUNT, "+ 
				 "PAYABLE_AMOUNT AS PAY_AMOUNT, "+
				 "SOURCE_FEES1, "+
				 "SOURCE_FEES2, "+
				 "SOURCE_FEES3, "+
				 "DEST_FEES1, "+
				 "DEST_FEES2, "+
				 "DEST_FEES3, "+
				 "DEST_MSISDN, "+
				 "MAKER_MEMO, "+
				 "PRINT_PERSON, "+
				 "REQUEST_DATE, "+
				 "REQUEST_STATUS, "+
				 "CURRENCY, "+
				 "MUS.USER_NAME AS MAKER_NAME, "+
				 "CUS.USER_NAME AS CHECKER_NAME, "+
				 "LUS.USER_NAME AS LAST_UPDATED_BY, "+
				 "AUTH_PERSON "+
				 "FROM "+env.getProperty("view.sales.requests")+" MCR " + 
				 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" MUS " + 
				 "ON MCR.MAKER_USER= MUS.USER_KEY " + 
				 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" CUS " + 
				 "ON MCR.CHECKER_USER= CUS.USER_KEY " + 
				 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" LUS " + 
				 "ON MCR.LAST_UPDATED_BY= LUS.USER_KEY "+
				 "WHERE (MCR.BULK_FLAG='N' OR MCR.BULK_FLAG IS NULL) " + 
				 "AND ((MCR.CHECKED_STATUS ='RQS' AND MCR.LAST_ACTION IS NULL) OR (MCR.CHECKED_STATUS <> 'RQS' AND MCR.LAST_ACTION='REJECT') OR (MCR.CHECKED_STATUS = 'Checked2' AND MCR.LAST_ACTION='APPROVE')) " +
				 "AND MCR.DEST_MSISDN LIKE :dest "+
				 "AND MCR.PRINT_PERSON = :username "+
				 "AND MCR.REQUEST_STATUS LIKE :status "+
				 "AND MCR.CURRENCY LIKE :currency "+
				 "AND (MCR.REQUEST_DATE >= To_date(:from,'yyyy-mm-dd') AND MCR.REQUEST_DATE <= To_date(:to,'yyyy-mm-dd'))  "+
				 "AND ((MCR.SOURCE_ACCOUNT = :source_id AND SERVICE_CODE <> 'PULLFLOAT') OR (MCR.DEST_ACCOUNT=:source_id AND SERVICE_CODE='PULLFLOAT')) ")
				.setParameter("username", username)
				.setParameter("source_id", source_id)
				.setParameter("dest", dest)
				.setParameter("from", from.toLocalDate().toString())
				.setParameter("to", to.toLocalDate().toString())
				.setParameter("status", status)
				.setParameter("currency", currency)
				.setFirstResult((pageNumber-1)*pageSize)
				.setMaxResults(pageSize)
				.getResultList();
	
		
		logger.info(String.format("Requests found =%d ... ", requests.size())); 
		
		List<Map<String,String>> result = new ArrayList<>();
		
		String lang = getUserLanguage(username);
		
		logger.info(String.format("User language found =%s ... ", lang)); 
		
		for(Object[] request : requests) {
			
			String svc_code = (String) request[1];
			
			if(svc_code.contentEquals("BZCASHOUT")) {
				
				result.add(extractCashoutPendingRequest(request,lang));
			}
			else {
				
				result.add(extractNormalPendingRequest(request,lang));
			}
  
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findPendingSalesRequests(List<String> privileges,
			String source_id,
			Integer pageNumber,
			Integer pageSize,
			String dest,
			LocalDateTime from,
			LocalDateTime to,
			String status,
			String currency,
			String username){
		
		/*
		 * Find pending approval requests only
		 */
		
		/*
		 * Updated 07062022 for multi checker feature
		 * 
		 * Added condition AND ((MCR.CHECKED_STATUS ='RQS' AND MCR.LAST_ACTION IS NULL AND MCR.CURRENT_APPROVER IN (:privileges)) OR (MCR.CHECKED_STATUS <> 'RQS' AND MCR.LAST_ACTION='REJECT') OR (MCR.CHECKED_STATUS = 'Checked2' AND MCR.LAST_ACTION='APPROVE'))
		 */
		
		
		List<Object[]> requests = entityManager.createNativeQuery(
				"SELECT REQUEST_ID, "+ 
				"SERVICE_CODE, "+
				"ORIGINAL_AMOUNT, "+
				"PAYABLE_AMOUNT, "+ 
				"PAYABLE_AMOUNT AS PAY_AMOUNT, "+
				"SOURCE_FEES1, "+
				"SOURCE_FEES2, "+
				"SOURCE_FEES3, "+
				"DEST_FEES1, "+
				"DEST_FEES2, "+
				"DEST_FEES3, "+
				"DEST_MSISDN, "+
				"MAKER_MEMO, "+
				"PRINT_PERSON, "+
				"REQUEST_DATE, "+
				"REQUEST_STATUS, "+
				"CURRENCY, "+
				 "MUS.USER_NAME AS MAKER_NAME, "+
				 "CUS.USER_NAME AS CHECKER_NAME, "+
				 "LUS.USER_NAME AS LAST_UPDATED_BY, "+
				 "AUTH_PERSON "+
				 "FROM "+env.getProperty("view.sales.requests")+" MCR " + 
				 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" MUS " + 
				 "ON MCR.MAKER_USER= MUS.USER_KEY " + 
				 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" CUS " + 
				 "ON MCR.CHECKER_USER= CUS.USER_KEY " + 
				 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" LUS " + 
				 "ON MCR.LAST_UPDATED_BY= LUS.USER_KEY "+ 
				 "WHERE (MCR.BULK_FLAG='N' OR MCR.BULK_FLAG IS NULL) " + 
				 "AND ((MCR.CHECKED_STATUS ='RQS' AND MCR.LAST_ACTION IS NULL AND MCR.CURRENT_APPROVER IN (:privileges)) OR (MCR.CHECKED_STATUS <> 'RQS' AND MCR.LAST_ACTION='REJECT') OR (MCR.CHECKED_STATUS = 'Checked2' AND MCR.LAST_ACTION='APPROVE')) " +
				 "AND MCR.DEST_MSISDN LIKE :dest "+
				 "AND MCR.REQUEST_STATUS LIKE :status "+
				 "AND MCR.CURRENCY LIKE :currency "+
				 "AND (MCR.REQUEST_DATE >= To_date(:from,'yyyy-mm-dd') AND MCR.REQUEST_DATE <= To_date(:to,'yyyy-mm-dd'))  "+
				 "AND ((MCR.SOURCE_ACCOUNT = :source_id AND SERVICE_CODE <> 'PULLFLOAT') OR (MCR.DEST_ACCOUNT=:source_id AND SERVICE_CODE='PULLFLOAT')) ")
				.setParameter("source_id", source_id)
				.setParameter("dest", dest)
				.setParameter("from", from.toLocalDate().toString())
				.setParameter("to", to.toLocalDate().toString())
				.setParameter("status", status)
				.setParameter("currency", currency)
				.setParameter("privileges", privileges)
				.setFirstResult((pageNumber-1)*pageSize)
				.setMaxResults(pageSize)
				.getResultList();
	
		
		logger.info(String.format("Requests found =%d ... ", requests.size())); 
		
		String lang = getUserLanguage(username);
		
		logger.info(String.format("User language found =%s ... ", lang)); 
		
		List<Map<String,String>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			//Map<String,String> r = new HashMap<>();
			
			String svc_code = (String) request[1];
			
			if(svc_code.contentEquals("BZCASHOUT")) {
				
				result.add(extractCashoutPendingRequest(request,lang));
			}
			else {
				
				result.add(extractNormalPendingRequest(request,lang));
			}
			
			
		    //result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findBulkRelatedSalesRequests(List<String> privileges,
			String source_id,
			Integer pageNumber,
			Integer pageSize,
			String bulkReference,
			String username){
		
		/*
		 * Find Bulk related SRs
		 */
		
		//Added "AND MCR.BULK_FLAG='N' " On 10042022
		//Change "AND MCR.BULK_REF = :bulkReference " instead of the requester remarks column
		
		List<Object[]> requests = entityManager.createNativeQuery(
				"SELECT REQUEST_ID, "+ 
						"SERVICE_CODE, "+
						"ORIGINAL_AMOUNT, "+
						"PAYABLE_AMOUNT, "+ 
						"PAYABLE_AMOUNT AS PAY_AMOUNT, "+
						"SOURCE_FEES1, "+
						"SOURCE_FEES2, "+
						"SOURCE_FEES3, "+
						"DEST_FEES1, "+
						"DEST_FEES2, "+
						"DEST_FEES3, "+
						"DEST_MSISDN, "+
						"MAKER_MEMO, "+
						"PRINT_PERSON, "+
						"REQUEST_DATE, "+
						"REQUEST_STATUS, "+
						"CURRENCY, "+
						 "MUS.USER_NAME AS MAKER_NAME, "+
						 "CUS.USER_NAME AS CHECKER_NAME, "+
						 "LUS.USER_NAME AS LAST_UPDATED_BY, "+
						 "AUTH_PERSON "+
						 "FROM "+env.getProperty("view.bulk.sales.requests")+" MCR " + 
						 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" MUS " + 
						 "ON MCR.MAKER_USER= MUS.USER_KEY " + 
						 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" CUS " + 
						 "ON MCR.CHECKER_USER= CUS.USER_KEY " + 
						 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" LUS " + 
						 "ON MCR.LAST_UPDATED_BY= LUS.USER_KEY "+  
				 "WHERE MCR.BULK_FLAG='Y' "+
				 "AND MCR.BULK_REF = :bulkReference "+
				 "AND ((MCR.SOURCE_ACCOUNT = :source_id AND SERVICE_CODE <> 'PULLFLOAT') OR (MCR.DEST_ACCOUNT=:source_id AND SERVICE_CODE='PULLFLOAT')) ")
				.setParameter("source_id", source_id)
				.setParameter("bulkReference", bulkReference)
				.setFirstResult((pageNumber-1)*pageSize)
				.setMaxResults(pageSize)
				.getResultList();
	
		
		logger.info(String.format("Requests found =%s ... ", requests.size())); 
		
		List<Map<String,String>> result = new ArrayList<>();
		
		String lang = getUserLanguage(username);
		
		logger.info(String.format("User language found =%s ... ", lang)); 
		
		for(Object[] request : requests) {
			
			//Map<String,String> r = new HashMap<>();
			
			String svc_code = (String) request[1];
			
			if(svc_code.contentEquals("BZCASHOUT")) {
				
				result.add(extractCashoutPendingRequest(request,lang));
			}
			else {
				
				result.add(extractNormalPendingRequest(request,lang));
			}
			
			
		    //result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	public Integer countPendingSalesRequests(String username, List<String> privileges,
			String source_id,
			String dest,
			LocalDateTime from,
			LocalDateTime to,
			String status,
			String currency){
		
		/*
		 * count pending approval requests only
		 */
		
		/*
		 * Updated 07062022 for multi checker feature
		 */
		
		Boolean self=false;
		
		for(String p:privileges) {
			
			if (p.contentEquals("WebMakerChekerViewSelf")) {
				
				self=true;
				break;
			}
			
		}
		
		if(self) {
			
			
			Object c = entityManager.createNativeQuery(
					"SELECT COUNT(*) "+ 
							"FROM "+env.getProperty("view.sales.requests")+" MCR " + 
							"WHERE (MCR.BULK_FLAG='N' OR MCR.BULK_FLAG IS NULL) " + 
							"AND (MCR.CHECKED_STATUS ='RQS' AND MCR.LAST_ACTION IS NULL) " +
							 "AND MCR.DEST_MSISDN LIKE :dest "+
							 "AND MCR.PRINT_PERSON = :username "+
							 "AND MCR.REQUEST_STATUS LIKE :status "+
							 "AND MCR.CURRENCY LIKE :currency "+
							 "AND (MCR.REQUEST_DATE >= To_date(:from,'yyyy-mm-dd') AND MCR.REQUEST_DATE <= To_date(:to,'yyyy-mm-dd'))  "+
							 "AND ((MCR.SOURCE_ACCOUNT = :source_id AND SERVICE_CODE <> 'PULLFLOAT') OR (MCR.DEST_ACCOUNT=:source_id AND SERVICE_CODE='PULLFLOAT')) ")
							.setParameter("username", username)
							.setParameter("source_id", source_id)
							.setParameter("dest", dest)
							.setParameter("from", from.toLocalDate().toString())
							.setParameter("to", to.toLocalDate().toString())
							.setParameter("status", status)
							.setParameter("currency", currency)
							.getSingleResult();
			
			
			
			if(c==null) {
				
				logger.info(String.format("Requests count =%s ... ", 0));
				return 0;
				
			}
			
			logger.info(String.format("Requests count =%s ... ", c));
				
			
		
			return Integer.parseInt(c.toString());
		}
		else {
			
			Object c = entityManager.createNativeQuery(
					"SELECT COUNT(*) "+ 
							"FROM "+env.getProperty("view.sales.requests")+" MCR " + 
							 "WHERE (MCR.BULK_FLAG='N' OR MCR.BULK_FLAG IS NULL) " + 
							 "AND (MCR.CHECKED_STATUS ='RQS' AND MCR.LAST_ACTION IS NULL) " +
							 "AND MCR.DEST_MSISDN LIKE :dest "+
							 "AND MCR.REQUEST_STATUS LIKE :status "+
							 "AND MCR.CURRENCY LIKE :currency "+
							 "AND (MCR.REQUEST_DATE >= To_date(:from,'yyyy-mm-dd') AND MCR.REQUEST_DATE <= To_date(:to,'yyyy-mm-dd'))  "+
							 "AND ((MCR.SOURCE_ACCOUNT = :source_id AND SERVICE_CODE <> 'PULLFLOAT') OR (MCR.DEST_ACCOUNT=:source_id AND SERVICE_CODE='PULLFLOAT')) ")
							.setParameter("source_id", source_id)
							.setParameter("dest", dest)
							.setParameter("from", from.toLocalDate().toString())
							.setParameter("to", to.toLocalDate().toString())
							.setParameter("status", status)
							.setParameter("currency", currency)
					.getSingleResult();
		
			
			
			
			if(c==null) {
				
				logger.info(String.format("Requests count =%s ... ", 0));
				return 0;
				
			}
			
			logger.info(String.format("Requests count =%s ... ", c));
				
			
		
			return Integer.parseInt(c.toString());
		}
		
	
	}
	
	public Integer countBulkRelatedSalesRequests(String source_id,
			String bulk_ref){
		
		/*
		 * count ALL related requests of a BULK request
		 * Regardless the status
		 */
		
		
		Object c = entityManager.createNativeQuery(
				"SELECT COUNT(*) "+ 
				 "FROM "+env.getProperty("view.bulk.sales.requests")+" MCR " + 
				 "WHERE MCR.BULK_FLAG='Y' "+
				 "AND MCR.BULK_REF = :bulkReference "+
				 "AND ((MCR.SOURCE_ACCOUNT = :source_id AND SERVICE_CODE <> 'PULLFLOAT') OR (MCR.DEST_ACCOUNT=:source_id AND SERVICE_CODE='PULLFLOAT')) ")
				.setParameter("source_id", source_id)
				.setParameter("bulkReference", bulk_ref)
				.getSingleResult();
	
		
		
		
		if(c==null) {
			
			logger.info(String.format("Requests count =%s ... ", 0));
			return 0;
			
		}
		
		logger.info(String.format("Requests count =%s ... ", c));
			
		
	
		return Integer.parseInt(c.toString());
	
	}
	
	public Integer countBulkRelatedSalesRequests(String source_id,
			String bulk_ref,
			String status){
		
		/*
		 * count Bulk related requests
		 * Given the status, to be used for summary reporting
		 */
		
		
		Object c = entityManager.createNativeQuery(
				"SELECT COUNT(*) "+ 
				 "FROM "+env.getProperty("view.bulk.sales.requests")+" MCR " + 
				 "WHERE MCR.BULK_FLAG='Y' "+
				 "AND MCR.BULK_REF = :bulkReference "+
				 "AND MCR.REQUEST_STATUS = :status "+
				 "AND ((MCR.SOURCE_ACCOUNT = :source_id AND SERVICE_CODE <> 'PULLFLOAT') OR (MCR.DEST_ACCOUNT=:source_id AND SERVICE_CODE='PULLFLOAT')) ")
				.setParameter("source_id", source_id)
				.setParameter("bulkReference", bulk_ref)
				.setParameter("status", status)
				.getSingleResult();
	
		
		
		
		if(c==null) {
			
			logger.info(String.format("Requests count =%s ... ", 0));
			return 0;
			
		}
		
		logger.info(String.format("Requests count =%s ... ", c));
			
		
	
		return Integer.parseInt(c.toString());
	
	}
			
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findSalesRequest(String request_id,String username){
		
		/*
		 * Updated 07062022 for multi checker feature
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				"SELECT REQUEST_ID, "+ 
						"SERVICE_CODE, "+
						"ORIGINAL_AMOUNT, "+
						"PAYABLE_AMOUNT, "+ 
						"PAYABLE_AMOUNT AS PAY_AMOUNT, "+
						"SOURCE_FEES1, "+
						"SOURCE_FEES2, "+
						"SOURCE_FEES3, "+
						"DEST_FEES1, "+
						"DEST_FEES2, "+
						"DEST_FEES3, "+
						"DEST_MSISDN, "+
						"MAKER_MEMO, "+
						"PRINT_PERSON, "+
						"REQUEST_DATE, "+
						"REQUEST_STATUS, "+
						"CURRENCY, "+
						"AUTH_PERSON, "+
						"AUTH_MOBILE, "+
						"AUTH_ID_NUMBER, "+
						"MUS.USER_NAME AS MAKER_NAME, "+
						 "CUS.USER_NAME AS CHECKER_NAME, "+
						 "LUS.USER_NAME AS LAST_UPDATED_BY, "+
						 "SOURCE_MSISDN "+
						 "FROM "+env.getProperty("view.sales.requests")+" MCR " + 
						 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" MUS " + 
						 "ON MCR.MAKER_USER= MUS.USER_KEY " + 
						 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" CUS " + 
						 "ON MCR.CHECKER_USER= CUS.USER_KEY " + 
						 "LEFT OUTER JOIN "+env.getProperty("view.users.all")+" LUS " + 
						 "ON MCR.LAST_UPDATED_BY= LUS.USER_KEY "+
				 "WHERE MCR.REQUEST_ID =:request_id "+
				 "AND ((MCR.CHECKED_STATUS ='RQS' AND MCR.LAST_ACTION IS NULL) OR (MCR.CHECKED_STATUS <> 'RQS' AND MCR.LAST_ACTION='REJECT') OR (MCR.CHECKED_STATUS = 'Checked2' AND MCR.LAST_ACTION='APPROVE')) ")
				.setParameter("request_id", request_id)
				.getResultList();
	
		
		List<Map<String,String>> result = new ArrayList<>();
		
		String lang = getUserLanguage(username);
		
		logger.info(String.format("User language found =%s ... ", lang)); 
		
		for(Object[] request : requests) {
			
			String svc_code = (String) request[1];
			
			if(svc_code.contentEquals("BZCASHOUT")) {
				
				result.add(extractCashoutRequest(request,lang));
			}
			else {
				
				result.add(extractNormalRequest(request,lang));
			}  
		    
		}
		
	
		return result;
	
	}
	
	private Map<String,String> extractNormalRequest(Object[] request,String lang){
		
		Map<String,String> r = new HashMap<>();
		
		r.put("Request_Id",String.format("%s", request[0]));
		r.put("Service",(String) request[1]);
		r.put("Original_Amount",String.format("%s", request[2]));
		r.put("Payable_Amount",String.format("%s", request[3]));
		r.put("Payment_Amount",String.format("%s", request[4]));
		r.put("Source_Fees1",String.format("%s", request[5]));
		r.put("Source_Fees2",String.format("%s", request[6]));
		r.put("Source_Fees3",String.format("%s", request[7]));
		r.put("Destination_Fees1",String.format("%s", request[8]));
		r.put("Destination_Fees2",String.format("%s", request[9]));
		r.put("Destination_Fees3",String.format("%s", request[10]));
		r.put("Destination_Account",(String) request[11]);		
		r.put("Requester_Remarks",(String) request[12]);
		r.put("Requested_By",(String) request[13]);
		r.put("Requested_On",String.format("%s", request[14]));
		r.put("Status",mapStatus(String.format("%s", request[15]),lang));
		r.put("Currency",String.format("%s", request[16]));
		r.put("Service_Name",mapServiceCode(request[1],lang));
		
		r.put("Authorized_Person","");
		r.put("Authorized_ID","");
		r.put("Authorized_Mobile","");
		
		r.put("Maker_Name",String.format("%s", request[20]));
		r.put("Checker_Name",String.format("%s", request[21]));
		r.put("Last_Updated_By",String.format("%s", request[22]));
		r.put("Source_Account",String.format("%s", request[23]));
		
		return r;
	}
	
	private Map<String,String> extractNormalPendingRequest(Object[] request,String lang){
		
		Map<String,String> r = new HashMap<>();
		
		r.put("Request_Id",String.format("%s", request[0]));
		r.put("Service",(String) request[1]);
		r.put("Original_Amount",String.format("%s", request[2]));
		r.put("Payable_Amount",String.format("%s", request[3]));
		r.put("Payment_Amount",String.format("%s", request[4]));
		r.put("Source_Fees1",String.format("%s", request[5]));
		r.put("Source_Fees2",String.format("%s", request[6]));
		r.put("Source_Fees3",String.format("%s", request[7]));
		r.put("Destination_Fees1",String.format("%s", request[8]));
		r.put("Destination_Fees2",String.format("%s", request[9]));
		r.put("Destination_Fees3",String.format("%s", request[10]));
		r.put("Destination_Account",(String) request[11]);		
		r.put("Requester_Remarks",(String) request[12]);
		r.put("Requested_By",(String) request[13]);
		r.put("Requested_On",String.format("%s", request[14]));
		r.put("Status",mapStatus(String.format("%s", request[15]),lang));
		r.put("Currency",String.format("%s", request[16]));
		r.put("Maker_Name",String.format("%s", request[17]));
		r.put("Checker_Name",String.format("%s", request[18]));
		r.put("Last_Updated_By",String.format("%s", request[19]));
		r.put("Service_Name",mapServiceCode(request[1],lang));
		
		return r;
	}
	
	private Map<String,String> extractCashoutRequest(Object[] request,String lang){
		
		Map<String,String> r = new HashMap<>();
		
		String data=(String) request[12];
		
		List<String> delegate_data = Arrays.asList(data.split("#", -1));
		
		r.put("Request_Id",String.format("%s", request[0]));
		r.put("Service",(String) request[1]);
		r.put("Original_Amount",String.format("%s", request[2]));
		r.put("Payable_Amount",String.format("%s", request[3]));
		r.put("Payment_Amount",String.format("%s", request[4]));
		r.put("Source_Fees1",String.format("%s", request[5]));
		r.put("Source_Fees2",String.format("%s", request[6]));
		r.put("Source_Fees3",String.format("%s", request[7]));
		r.put("Destination_Fees1",String.format("%s", request[8]));
		r.put("Destination_Fees2",String.format("%s", request[9]));
		r.put("Destination_Fees3",String.format("%s", request[10]));
		r.put("Destination_Account",(String) request[11]);		
		r.put("Requester_Remarks",(String) request[17]);
		r.put("Requested_By",(String) request[13]);
		r.put("Requested_On",String.format("%s", request[14]));
		r.put("Status",mapStatus(String.format("%s", request[15]),lang));
		r.put("Currency",String.format("%s", request[16]));
		r.put("Service_Name",mapServiceCode(request[1],lang));
		
		r.put("Authorized_Person",delegate_data.get(0));
		r.put("Authorized_ID",delegate_data.get(1));
		r.put("Authorized_Mobile",delegate_data.get(2));
		
		r.put("Maker_Name",String.format("%s", request[20]));
		r.put("Checker_Name",String.format("%s", request[21]));
		r.put("Last_Updated_By",String.format("%s", request[22]));
		r.put("Source_Account",String.format("%s", request[23]));
		
		return r;
	}
	
	private Map<String,String> extractCashoutPendingRequest(Object[] request,String lang){
		
		Map<String,String> r = new HashMap<>();
		
		r.put("Request_Id",String.format("%s", request[0]));
		r.put("Service",(String) request[1]);
		r.put("Original_Amount",String.format("%s", request[2]));
		r.put("Payable_Amount",String.format("%s", request[3]));
		r.put("Payment_Amount",String.format("%s", request[4]));
		r.put("Source_Fees1",String.format("%s", request[5]));
		r.put("Source_Fees2",String.format("%s", request[6]));
		r.put("Source_Fees3",String.format("%s", request[7]));
		r.put("Destination_Fees1",String.format("%s", request[8]));
		r.put("Destination_Fees2",String.format("%s", request[9]));
		r.put("Destination_Fees3",String.format("%s", request[10]));
		r.put("Destination_Account",(String) request[11]);		
		r.put("Requester_Remarks",(String) request[20]);//This is the AUTH_PERSON column
		r.put("Requested_By",(String) request[13]);
		r.put("Requested_On",String.format("%s", request[14]));
		r.put("Status",mapStatus(String.format("%s", request[15]),lang));
		r.put("Currency",String.format("%s", request[16]));
		r.put("Maker_Name",String.format("%s", request[17]));
		r.put("Checker_Name",String.format("%s", request[18]));
		r.put("Last_Updated_By",String.format("%s", request[19]));
		r.put("Service_Name",mapServiceCode(request[1],lang));
		
		return r;
	}
	
	private String mapStatus(String status,String lang) {
		
		try {
			
			if(lang.contentEquals("Arabic")) {
				
				switch(status) {
				
				case "RQS":
					return "بانتظار الموافقة";
				case "PST":
					return "تمت  الموافقة";
				case "DCL":
					return "تم  الرفض";
					default:
						return "غير معرف";
				}
			}
			else {
				
				switch(status) {
				
				case "RQS":
					return "Pending";
				case "PST":
					return "Posted";
				case "DCL":
					return "Rejected";
					default:
						return "Unknown";
				}
			}
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			return "Unknown - غير معرف";
		}
		
	}

private String mapServiceCode(Object o,String lang) {
		

		try {
			
			if(lang.contentEquals("Arabic")) {
				
				switch(o.toString()) {
				
				case "PUSHFLOAT":
					return env.getProperty("service.name.pushfloat.ar");
				case "PULLFLOAT":
					return env.getProperty("service.name.pullfloat.ar");
				case "SELLFLOAT":
					return env.getProperty("service.name.sellfloat.ar");
				case "MONEYUNALLOC":
					return env.getProperty("service.name.deletemoney.ar");
				case "BZCASHIN":
					return env.getProperty("service.name.bcashin.ar");
				case "BZCASHOUT":
					return env.getProperty("service.name.bcashout.ar");
				case "M2B":
					return env.getProperty("service.name.m2b.ar");
				case "M2M":
					return env.getProperty("service.name.m2m.ar");
				case "M2P":
					return env.getProperty("service.name.m2p.ar");
				case "tcs.payment.m2p.voucher.send":
					return env.getProperty("service.name.m2p.ar");
				case "B2B":
					return env.getProperty("service.name.b2b.ar");
				case "B2M":
					return env.getProperty("service.name.b2m.ar");
				case "B2P":
					return env.getProperty("service.name.b2p.ar");
				case "tcs.payment.b2p.voucher.send":
					return env.getProperty("service.name.b2p.ar");
				case "tcs.payment.customer.cashin":
					return env.getProperty("service.name.ccashin.ar");
				case "tcs.payment.customer.redeem":
					return env.getProperty("service.name.credeem.ar");
					default:
						return "غير معرف";
				}
			}
			else {
				
				switch(o.toString()) {
				
				case "PUSHFLOAT":
					return env.getProperty("service.name.pushfloat.en");
				case "PULLFLOAT":
					return env.getProperty("service.name.pullfloat.en");
				case "SELLFLOAT":
					return env.getProperty("service.name.sellfloat.en");
				case "MONEYUNALLOC":
					return env.getProperty("service.name.deletemoney.en");
				case "BZCASHIN":
					return env.getProperty("service.name.bcashin.en");
				case "BZCASHOUT":
					return env.getProperty("service.name.bcashout.en");
				case "M2B":
					return env.getProperty("service.name.m2b.en");
				case "M2M":
					return env.getProperty("service.name.m2m.en");
				case "M2P":
					return env.getProperty("service.name.m2p.en");
				case "tcs.payment.m2p.voucher.send":
					return env.getProperty("service.name.m2p.en");
				case "B2B":
					return env.getProperty("service.name.b2b.en");
				case "B2M":
					return env.getProperty("service.name.b2m.en");
				case "B2P":
					return env.getProperty("service.name.b2p.en");
				case "tcs.payment.b2p.voucher.send":
					return env.getProperty("service.name.b2p.en");
				case "tcs.payment.customer.cashin":
					return env.getProperty("service.name.ccashin.en");
				case "tcs.payment.customer.redeem":
					return env.getProperty("service.name.credeem.en");
					default:
						return "Unknown";
				}
			}
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			return "Unknown - غير معرف";
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
}
