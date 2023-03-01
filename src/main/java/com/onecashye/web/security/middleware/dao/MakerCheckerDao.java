package com.onecashye.web.security.middleware.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;



@Repository
public class MakerCheckerDao {

	Logger logger = LogManager.getLogger(MakerCheckerDao.class);
	
	private final EntityManager entityManager;
	
	
	@Autowired
	public MakerCheckerDao(@Qualifier("middlewareEM") EntityManager entityManager) {
		
		this.entityManager=entityManager;
	}
	
	@Deprecated
	@Transactional(value="mwTransactionManager",readOnly = false)
	public String insertSalesRequest(Map<String,Object> entity) {
		
		//Change: Added Column (CURRENCY) to insert statement
		//Change: Added Column (BULK_FLAG) to insert statement
		
		entityManager.createNativeQuery(
			    "INSERT INTO `web_maker_checker_request` (" + 
			    "	`TCS_REQUEST_ID` ," + 
			    "	`ORIGINAL_AMOUNT` ," + 
			    "	`PAYABLE_AMOUNT` ," + 
			    "	`PAYMENT_AMOUNT` ," + 
			    "	`SOURCE_FEES1` ," + 
			    "	`SOURCE_FEES2` ," + 
			    "	`SOURCE_FEES3` ," + 
			    "	`DESTINATION_FEES1` ," + 
			    "	`DESTINATION_FEES2` ," + 
			    "	`DESTINATION_FEES3` ," + 
			    "	`SOURCE_ACCOUNT` ," + 
			    "	`DESTINATION_ACCOUNT` ," + 
			    "	`SERVICE_CODE` ," + 
			    "	`BRAND_ID` ," + 
			    "	`REQUESTER_REMARKS` ," + 
			    "	`REQUESTED_BY` ," +
			    "	`REQUESTED_ON` ," +
			    "	`SOURCE_ID` ," +
			    "	`CURRENT_STATUS` ," +
			    "	`CURRENCY` ," +
			    "	`BULK_FLAG` ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" )
				.setParameter(1, entity.get("Request_ID"))
				.setParameter(2, entity.get("Original_Amount"))
				.setParameter(3, entity.get("Payable_Amount"))
				.setParameter(4, entity.get("Payment_Amount"))
				.setParameter(5, entity.get("Source_Fees1"))
				.setParameter(6, entity.get("Source_Fees2"))
				.setParameter(7, entity.get("Source_Fees3"))
				.setParameter(8, entity.get("Destination_Fees1"))
				.setParameter(9, entity.get("Destination_Fees2"))
				.setParameter(10, entity.get("Destination_Fees3"))
				.setParameter(11, entity.get("Source_Account"))
				.setParameter(12, entity.get("Destination_Account"))
				.setParameter(13, entity.get("Service_Code"))
				.setParameter(14, entity.get("Brand_Id"))
				.setParameter(15, entity.get("Requester_Remarks"))
				.setParameter(16, entity.get("Requested_By"))
				.setParameter(17, Timestamp.valueOf(LocalDateTime.now()))
				.setParameter(18, entity.get("Source_Id"))
				.setParameter(19, "RQS")
				.setParameter(20, entity.get("Currency"))
				.setParameter(21, entity.get("Bulk_Flag"))
				.executeUpdate();
		
		String lastId = entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult().toString();
		return lastId;
		
		
	}
	
	@Deprecated
	@Transactional(value="mwTransactionManager",readOnly = false)
	public String insertBulkSalesRequest(Map<String,Object> entity) {
		
		//Change: Added Column (CURRENCY) to insert statement
		//Change: Added Column (BULK_FLAG) to insert statement
		//Change: Added Column (BULK_REF) to insert statement
		
		entityManager.createNativeQuery(
			    "INSERT INTO `web_maker_checker_request` (" + 
			    "	`TCS_REQUEST_ID` ," + 
			    "	`ORIGINAL_AMOUNT` ," + 
			    "	`PAYABLE_AMOUNT` ," + 
			    "	`PAYMENT_AMOUNT` ," + 
			    "	`SOURCE_FEES1` ," + 
			    "	`SOURCE_FEES2` ," + 
			    "	`SOURCE_FEES3` ," + 
			    "	`DESTINATION_FEES1` ," + 
			    "	`DESTINATION_FEES2` ," + 
			    "	`DESTINATION_FEES3` ," + 
			    "	`SOURCE_ACCOUNT` ," + 
			    "	`DESTINATION_ACCOUNT` ," + 
			    "	`SERVICE_CODE` ," + 
			    "	`BRAND_ID` ," + 
			    "	`REQUESTER_REMARKS` ," + 
			    "	`REQUESTED_BY` ," +
			    "	`REQUESTED_ON` ," +
			    "	`SOURCE_ID` ," +
			    "	`CURRENT_STATUS` ," +
			    "	`CURRENCY` ," +
			    "	`BULK_FLAG` ," +
			    "	`BULK_REF` ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" )
				.setParameter(1, entity.get("Request_ID"))
				.setParameter(2, entity.get("Original_Amount"))
				.setParameter(3, entity.get("Payable_Amount"))
				.setParameter(4, entity.get("Payment_Amount"))
				.setParameter(5, entity.get("Source_Fees1"))
				.setParameter(6, entity.get("Source_Fees2"))
				.setParameter(7, entity.get("Source_Fees3"))
				.setParameter(8, entity.get("Destination_Fees1"))
				.setParameter(9, entity.get("Destination_Fees2"))
				.setParameter(10, entity.get("Destination_Fees3"))
				.setParameter(11, entity.get("Source_Account"))
				.setParameter(12, entity.get("Destination_Account"))
				.setParameter(13, entity.get("Service_Code"))
				.setParameter(14, entity.get("Brand_Id"))
				.setParameter(15, entity.get("Requester_Remarks"))
				.setParameter(16, entity.get("Requested_By"))
				.setParameter(17, Timestamp.valueOf(LocalDateTime.now()))
				.setParameter(18, entity.get("Source_Id"))
				.setParameter(19, "RQS")
				.setParameter(20, entity.get("Currency"))
				.setParameter(21, entity.get("Bulk_Flag"))
				.setParameter(22, entity.get("Bulk_Ref"))
				.executeUpdate();
		
		String lastId = entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult().toString();
		return lastId;
		
		
	}
	
	@Deprecated
	@Transactional(value="mwTransactionManager",readOnly = false)
	public String insertBusinessCashoutRequest(Map<String,Object> entity) {
		
		entityManager.createNativeQuery(
			    "INSERT INTO `web_maker_checker_request` (" + 
			    "	`TCS_REQUEST_ID` ," + 
			    "	`ORIGINAL_AMOUNT` ," + 
			    "	`PAYABLE_AMOUNT` ," + 
			    "	`PAYMENT_AMOUNT` ," + 
			    "	`SOURCE_FEES1` ," + 
			    "	`SOURCE_FEES2` ," + 
			    "	`SOURCE_FEES3` ," + 
			    "	`DESTINATION_FEES1` ," + 
			    "	`DESTINATION_FEES2` ," + 
			    "	`DESTINATION_FEES3` ," + 
			    "	`SOURCE_ACCOUNT` ," + 
			    "	`DESTINATION_ACCOUNT` ," + 
			    "	`SERVICE_CODE` ," + 
			    "	`BRAND_ID` ," + 
			    "	`REQUESTER_REMARKS` ," + 
			    "	`REQUESTED_BY` ," +
			    "	`REQUESTED_ON` ," +
			    "	`SOURCE_ID` ," +
			    "	`CURRENT_STATUS` ," +
			    "	`AUTH_PERSON` ," +
			    "	`AUTH_ID_NUMBER` ," +
			    "	`AUTH_MOBILE` ," +
			    "	`CURRENCY` ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" )
				.setParameter(1, entity.get("Request_ID"))
				.setParameter(2, entity.get("Original_Amount"))
				.setParameter(3, entity.get("Payable_Amount"))
				.setParameter(4, entity.get("Payment_Amount"))
				.setParameter(5, entity.get("Source_Fees1"))
				.setParameter(6, entity.get("Source_Fees2"))
				.setParameter(7, entity.get("Source_Fees3"))
				.setParameter(8, entity.get("Destination_Fees1"))
				.setParameter(9, entity.get("Destination_Fees2"))
				.setParameter(10, entity.get("Destination_Fees3"))
				.setParameter(11, entity.get("Source_Account"))
				.setParameter(12, entity.get("Destination_Account"))
				.setParameter(13, entity.get("Service_Code"))
				.setParameter(14, entity.get("Brand_Id"))
				.setParameter(15, entity.get("Requester_Remarks"))
				.setParameter(16, entity.get("Requested_By"))
				.setParameter(17, Timestamp.valueOf(LocalDateTime.now()))
				.setParameter(18, entity.get("Source_Id"))
				.setParameter(19, "RQS")
				.setParameter(20, entity.get("Authorized_Person"))
				.setParameter(21, entity.get("Authorized_ID"))
				.setParameter(22, entity.get("Authorized_Mobile"))
				.setParameter(23, entity.get("Currency"))
				.executeUpdate();
		
		String lastId = entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult().toString();
		return lastId;
		
		
	}
	
	@Deprecated
	@Transactional(value="mwTransactionManager",readOnly = false)
	public String approveSalesRequest(Map<String,Object> entity) {
		
		Query q = entityManager.createNativeQuery(
			    "UPDATE web_maker_checker_request AS MCR " + 
			    "SET MCR.CHECKER_ACTION= :checker_action , " + 
			    "	 MCR.CURRENT_STATUS= :status , " + 
			    "	 MCR.CHECKER_REMARKS= :remarks , " + 
			    "	 MCR.TCS_TRX_ID= :tcs_trx_id , " +
			    "	 MCR.UPDATED_BY= :updated_by , " + 
			    "	 MCR.UPDATED_ON= :updated_on  " + 
			    "WHERE MCR.TCS_REQUEST_ID= :request_id " )
				.setParameter("request_id", entity.get("Request_ID"))
				.setParameter("checker_action", entity.get("Checker_Action"))
				.setParameter("tcs_trx_id", entity.get("Transaction_ID"))
				.setParameter("status", entity.get("Status"))
				.setParameter("updated_by", entity.get("Updated_By"))
				.setParameter("updated_on", Timestamp.valueOf(LocalDateTime.now()));
		
		if(entity.get("Remarks").toString().isEmpty()) {
			
			q.setParameter("remarks", "N/A");
		}
		else {
			
			q.setParameter("remarks", entity.get("Remarks").toString());
		}
		
		Integer count=q.executeUpdate();
		
		if(count != null && count==1) {
			
			return count.toString();
		}
			
		return "";
		
		
	}
	
	@Deprecated
	@Transactional(value="mwTransactionManager",readOnly = false)
	public String rejectSalesRequest(Map<String,Object> entity) {
		
		Query q = entityManager.createNativeQuery(
			    "UPDATE web_maker_checker_request AS MCR " + 
			    "SET MCR.CHECKER_ACTION= :checker_action ," + 
			    "	 MCR.CURRENT_STATUS= :status , " + 
			    "	 MCR.CHECKER_REMARKS= :remarks , " + 
			    "	 MCR.UPDATED_BY= :updated_by , " + 
			    "	 MCR.UPDATED_ON= :updated_on  " + 
			    "WHERE MCR.TCS_REQUEST_ID= :request_id " )
				.setParameter("request_id", entity.get("Request_ID"))
				.setParameter("checker_action", entity.get("Checker_Action"))
				.setParameter("status", entity.get("Status"))
				.setParameter("updated_by", entity.get("Updated_By"))
				.setParameter("updated_on", Timestamp.valueOf(LocalDateTime.now()));
		
		if(entity.get("Remarks").toString().isEmpty()) {
			
			q.setParameter("remarks", "N/A");
		}
		else {
			
			q.setParameter("remarks", entity.get("Remarks").toString());
		}
		
		Integer count=q.executeUpdate();
		
		if(count != null && count==1) {
			
			return count.toString();
		}
			
		return "";
		
		
	}

	@Deprecated
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
		
		//Remove Privilege Condition
		//Added "AND MCR.BULK_FLAG='N' " On 10042022
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM V$SALES_REQUEST_DETAILS AS MCR " + 
				 "WHERE MCR.BULK_FLAG='N' " + 
				 "AND MCR.DEST_ACCOUNT LIKE :dest "+
				 "AND MCR.PRINT_PERSON LIKE :username "+
				 "AND MCR.REQUEST_STATUS LIKE :status "+
				 "AND MCR.CURRENCY LIKE :currency "+
				 "AND (MCR.REQUEST_DATE >= :from AND MCR.REQUEST_DATE <= :to) "+
				 "AND MCR.SOURCE_ACCOUNT = :source_id ")
				.setParameter("username", username)
				.setParameter("source_id", source_id)
				.setParameter("dest", dest)
				.setParameter("from", from)
				.setParameter("to", to)
				.setParameter("status", status)
				.setParameter("currency", currency)
				.setFirstResult((pageNumber-1)*pageSize)
				.setMaxResults(pageSize)
				.getResultList();
	
		
		logger.info(String.format("Requests found =%d ... ", requests.size())); 
		
		List<Map<String,String>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,String> r = new HashMap<>();
			
			try {
				
				r.put("Request_Id",(String) request[1]);
				r.put("Original_Amount",(String) request[2]);
				r.put("Payable_Amount",(String) request[3]);
				r.put("Payment_Amount",(String) request[4]);
				r.put("Source_Fees1",(String) request[5]);
				r.put("Source_Fees2",(String) request[6]);
				r.put("Source_Fees3",(String) request[7]);
				r.put("Destination_Fees1",(String) request[8]);
				r.put("Destination_Fees2",(String) request[9]);
				r.put("Destination_Fees3",(String) request[10]);
				r.put("Destination_Account",(String) request[13]);
				r.put("Service",(String) request[14]);
				r.put("Requester_Remarks",(String) request[16]);
				r.put("Requested_By",(String) request[17]);
				r.put("Requested_On",String.format("%s", request[18]));
				r.put("Status",mapStatus(String.format("%s", request[24])));//Added on 12-03-2022 //Modified 06042022
				r.put("Currency",String.format("%s", request[26]));//Added on 11-03-2022
				r.put("Service_Name",String.format("%s", request[27]));//Added on 22-03-2022
			}
			catch(Exception e) {
				
				e.printStackTrace();
			}
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@Deprecated
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findPendingSalesRequests(List<String> privileges,
			String source_id,
			Integer pageNumber,
			Integer pageSize,
			String dest,
			LocalDateTime from,
			LocalDateTime to,
			String status,
			String currency){
		
		/*
		 * Find pending approval requests only
		 */
		//Updated 06042022
		//Remove Privilege Condition
		//Added "AND MCR.BULK_FLAG='N' " On 10042022
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM web_maker_checker_request_pending_v AS MCR " + 
				 "WHERE MCR.PRIV LIKE '%' " + //"WHERE MCR.PRIV IN (:privileges) " + 
				 "AND MCR.BULK_FLAG='N' "+
				 "AND MCR.DESTINATION_ACCOUNT LIKE :dest "+
				 "AND MCR.CURRENT_STATUS LIKE :status "+
				 "AND MCR.CURRENCY LIKE :currency "+
				 "AND (MCR.REQUESTED_ON >= :from AND MCR.REQUESTED_ON <= :to) "+
				 "AND MCR.SOURCE_ID = :source_id ")
				//.setParameter("privileges", privileges)
				.setParameter("source_id", source_id)
				.setParameter("dest", dest)
				.setParameter("from", from)
				.setParameter("to", to)
				.setParameter("status", status)
				.setParameter("currency", currency)
				.setFirstResult((pageNumber-1)*pageSize)
				.setMaxResults(pageSize)
				.getResultList();
	
		
		logger.info(String.format("Requests found =%d ... ", requests.size())); 
		
		List<Map<String,String>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,String> r = new HashMap<>();
			
			try {
				
				r.put("Request_Id",(String) request[1]);
				r.put("Original_Amount",(String) request[2]);
				r.put("Payable_Amount",(String) request[3]);
				r.put("Payment_Amount",(String) request[4]);
				r.put("Source_Fees1",(String) request[5]);
				r.put("Source_Fees2",(String) request[6]);
				r.put("Source_Fees3",(String) request[7]);
				r.put("Destination_Fees1",(String) request[8]);
				r.put("Destination_Fees2",(String) request[9]);
				r.put("Destination_Fees3",(String) request[10]);
				r.put("Destination_Account",(String) request[13]);
				r.put("Service",(String) request[14]);
				r.put("Requester_Remarks",(String) request[16]);
				r.put("Requested_By",(String) request[17]);
				r.put("Requested_On",String.format("%s", request[18]));
				r.put("Status",mapStatus(String.format("%s", request[24])));//Added on 12-03-2022 //Modified 06042022
				r.put("Currency",String.format("%s", request[26]));//Added on 11-03-2022
				r.put("Service_Name",String.format("%s", request[27]));//Added on 22-03-2022
			}
			catch(Exception e) {
				
				e.printStackTrace();
			}
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@Deprecated
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findBulkRelatedSalesRequests(List<String> privileges,
			String source_id,
			Integer pageNumber,
			Integer pageSize,
			String bulkReference){
		
		/*
		 * Find Bulk related SRs
		 */
		
		//Added "AND MCR.BULK_FLAG='N' " On 10042022
		//Change "AND MCR.BULK_REF = :bulkReference " instead of the requester remarks column
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM web_maker_checker_request_pending_v AS MCR " + 
				 "WHERE MCR.PRIV LIKE '%' " + 
				 "AND MCR.BULK_FLAG='Y' "+
				 "AND MCR.BULK_REF = :bulkReference "+
				 "AND MCR.SOURCE_ID = :source_id ")
				.setParameter("source_id", source_id)
				.setParameter("bulkReference", bulkReference)
				.setFirstResult((pageNumber-1)*pageSize)
				.setMaxResults(pageSize)
				.getResultList();
	
		
		logger.info(String.format("Requests found =%d ... ", requests.size())); 
		
		List<Map<String,String>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,String> r = new HashMap<>();
			
			try {
				
				r.put("Request_Id",(String) request[1]);
				r.put("Original_Amount",(String) request[2]);
				r.put("Payable_Amount",(String) request[3]);
				r.put("Payment_Amount",(String) request[4]);
				r.put("Source_Fees1",(String) request[5]);
				r.put("Source_Fees2",(String) request[6]);
				r.put("Source_Fees3",(String) request[7]);
				r.put("Destination_Fees1",(String) request[8]);
				r.put("Destination_Fees2",(String) request[9]);
				r.put("Destination_Fees3",(String) request[10]);
				r.put("Destination_Account",(String) request[13]);
				r.put("Service",(String) request[14]);
				r.put("Requester_Remarks",(String) request[16]);
				r.put("Requested_By",(String) request[17]);
				r.put("Requested_On",String.format("%s", request[18]));
				r.put("Status",mapStatus(String.format("%s", request[24])));//Added on 12-03-2022 //Modified 06042022
				r.put("Currency",String.format("%s", request[26]));//Added on 11-03-2022
				r.put("Service_Name",String.format("%s", request[27]));//Added on 22-03-2022
			}
			catch(Exception e) {
				
				e.printStackTrace();
			}
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@Deprecated
	public Integer countPendingSalesRequests(List<String> privileges,
			String source_id,
			String dest,
			Date from,
			Date to,
			String status,
			String currency){
		
		/*
		 * count pending approval requests only
		 */
		
		Object c = entityManager.createNativeQuery(
				 "SELECT COUNT(*) "+ 
				 "FROM web_maker_checker_request_pending_v AS MCR " + 
				 "WHERE MCR.PRIV LIKE '%' " + //"WHERE MCR.PRIV IN (:privileges) " +
				 "AND MCR.BULK_FLAG='N' "+
				 "AND MCR.DESTINATION_ACCOUNT LIKE :dest "+
				 "AND MCR.CURRENT_STATUS LIKE :status "+
				 "AND MCR.CURRENCY LIKE :currency "+
				 "AND (MCR.REQUESTED_ON > :from AND MCR.REQUESTED_ON < :to) "+
				 "AND MCR.SOURCE_ID = :source_id ")
				//.setParameter("privileges", privileges)
				.setParameter("source_id", source_id)
				.setParameter("dest", dest)
				.setParameter("from", from)
				.setParameter("to", to)
				.setParameter("status", status)
				.setParameter("currency", currency)
				.getSingleResult();
	
		
		
		
		if(c==null) {
			
			logger.info(String.format("Requests count =%d ... ", 0));
			return 0;
			
		}
		
		logger.info(String.format("Requests count =%d ... ", c));
			
		
	
		return Integer.parseInt(c.toString());
	
	}
	
	@Deprecated
	public Integer countBulkRelatedSalesRequests(List<String> privileges,
			String source_id,
			String bulk_ref,
			String status){
		
		/*
		 * count Bulk related requests
		 */
		//Change 18042022, "AND MCR.BULK_REF = :bulk_ref "+ instead of requester remarks column
		
		Object c = entityManager.createNativeQuery(
				 "SELECT COUNT(*) "+ 
				 "FROM web_maker_checker_request_pending_v AS MCR " + 
				 "WHERE MCR.PRIV LIKE '%' " + //"WHERE MCR.PRIV IN (:privileges) " +
				 "AND MCR.BULK_FLAG='Y' "+
				 "AND MCR.BULK_REF = :bulk_ref "+
				 "AND MCR.CURRENT_STATUS LIKE :status "+
				 "AND MCR.SOURCE_ID = :source_id ")
				//.setParameter("privileges", privileges)
				.setParameter("source_id", source_id)
				.setParameter("bulk_ref", bulk_ref)
				.setParameter("status", status)
				.getSingleResult();
	
		
		
		
		if(c==null) {
			
			logger.info(String.format("Requests count =%d ... ", 0));
			return 0;
			
		}
		
		logger.info(String.format("Requests count =%d ... ", c));
			
		
	
		return Integer.parseInt(c.toString());
	
	}
		
	@Deprecated
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,String>> findSalesRequest(String request_id){
		
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM V$SALES_REQUEST_DETAILS AS MCR " + 
				 "WHERE MCR.REQUEST_ID =:request_id ")
				.setParameter("request_id", request_id)
				.getResultList();
	
		
		List<Map<String,String>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,String> r = new HashMap<>();
			
			r.put("Request_Id",(String) request[1]);
			r.put("Original_Amount",(String) request[2]);
			r.put("Payable_Amount",(String) request[3]);
			r.put("Payment_Amount",(String) request[4]);
			r.put("Source_Fees1",(String) request[5]);
			r.put("Source_Fees2",(String) request[6]);
			r.put("Source_Fees3",(String) request[7]);
			r.put("Destination_Fees1",(String) request[8]);
			r.put("Destination_Fees2",(String) request[9]);
			r.put("Destination_Fees3",(String) request[10]);
			r.put("Destination_Account",(String) request[13]);
			r.put("Service",(String) request[14]);
			r.put("Requester_Remarks",(String) request[16]);
			r.put("Requested_By",(String) request[17]);
			r.put("Requested_On",request[18].toString());
			r.put("Authorized_Person",(String) request[25]);
			r.put("Authorized_ID",(String) request[26]);
			r.put("Authorized_Mobile",(String) request[27]);
			r.put("Currency",request[28].toString());//Added on 11-03-2022
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	
	private String mapStatus(String status) {
		
		try {
			
			switch(status) {
			
			case "RQS":
				return "Pending - بانتظار الموافقة";
			case "PST":
				return "Posted - تمت  الموافقة";
			case "DCL":
				return "Rejected - تم  الرفض";
				default:
					return "Unknown - غير معرف";
			}
		}
		catch(Exception e) {
			
			e.printStackTrace();
			return "Unknown - غير معرف";
		}
		
	}
	
	@Transactional(value="mwTransactionManager",readOnly = false)
	public Integer createFirstSalesRequestCheckerLine(Map<String,Object> entity) {
		
		/*
		 * 04062022
		 * 
		 * For multi checker business flow
		 * 
		 * Only to be used when a sales request is created for the first time
		 */
		
	Integer i =entityManager.createNativeQuery(
			    "INSERT INTO `web_sales_request_checker` (" + 
			    "	`REQUEST_ID` ," + 
			    "	`CHECKER_ACCOUNT_ID` ," + 
			    "	`CHECKER_USER_ID` ," + 
			    "	`CHECKED_STATUS` ," + 
			    "	`CHECKER_REFERENCE` ," + 
			    "	`CHECKER_REMARK` ," + 
			    "	`CHECKER_PERIORITY` ," + 
			    "	`CURRENT_APPROVER` ," + 
			    "	`LAST_ACTION` " + 
			    " ) VALUES (?,?,?,?,?,?,?,?,?)" )
				.setParameter(1, entity.get("Request_ID"))
				.setParameter(2, entity.get("Account_Id"))
				.setParameter(3, null)
				.setParameter(4, "RQS")
				.setParameter(5, null)
				.setParameter(6, null)
				.setParameter(7, 1)//Priority is always 1 when first creation
				.setParameter(8, "WebChecker1")
				.setParameter(9, null)
				.executeUpdate();
		
		return i;
		
		
	}
	
	@Transactional(value="mwTransactionManager",readOnly = false)
	public Integer createSecondSalesRequestCheckerLine(Map<String,Object> entity) {
		
		/*
		 * 04062022
		 * 
		 * For multi checker business flow
		 * 
		 * Only to be used when a sales request is created for the first time
		 */
		
		Integer i =entityManager.createNativeQuery(
			    "INSERT INTO `web_sales_request_checker` (" + 
			    "	`REQUEST_ID` ," + 
			    "	`CHECKER_ACCOUNT_ID` ," + 
			    "	`CHECKER_USER_ID` ," + 
			    "	`CHECKED_STATUS` ," + 
			    "	`CHECKER_REFERENCE` ," + 
			    "	`CHECKER_REMARK` ," + 
			    "	`CHECKER_PERIORITY` ," + 
			    "	`CURRENT_APPROVER` ," + 
			    "	`LAST_ACTION` " + 
			    " ) VALUES (?,?,?,?,?,?,?,?,?)" )
				.setParameter(1, entity.get("request_id"))
				.setParameter(2, entity.get("account_id"))
				.setParameter(3, null)
				.setParameter(4, "RQS")
				.setParameter(5, null)
				.setParameter(6, null)
				.setParameter(7, 2)//Priority is always 2 when second creation
				.setParameter(8, "WebChecker2")
				.setParameter(9, null)
				.executeUpdate();
		
		return i;
		
		
	}
	
	@Transactional(value="mwTransactionManager",readOnly = false)
	public Integer updateSalesRequestCheckerLine(Map<String,Object> entity) {
		
		Query q = entityManager.createNativeQuery(
			    "UPDATE web_sales_request_checker AS MCR " + 
			    "SET MCR.LAST_ACTION= :checker_action ," + 
			    "	 MCR.CHECKER_USER_ID= :updated_by , " + 
			    "	 MCR.CHECKED_STATUS= :status , " +
			    "	 MCR.CHECKER_REMARK= :remarks , " +
			    "	 MCR.CHECKER_REFERENCE= :reference , " +
			    "	 MCR.CHECKED_DATE= :updated_on  " + 
			    "WHERE MCR.REQUEST_ID= :request_id " +
			    "AND MCR.CHECKED_STATUS='RQS' "+
			    "AND MCR.CURRENT_APPROVER= :checker_privilege ")
				.setParameter("request_id", entity.get("request_id"))
				.setParameter("checker_action", entity.get("action"))
				.setParameter("status", entity.get("status"))
				.setParameter("remarks", entity.get("remarks"))
				.setParameter("reference", entity.get("reference"))
				.setParameter("updated_by", entity.get("user_id"))
				.setParameter("checker_privilege", entity.get("checker_privilege"))
				.setParameter("updated_on", Timestamp.valueOf(LocalDateTime.now()));
		
		Integer count=q.executeUpdate();
		
		return count;
		
		
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> findSalesRequestCheckerLine(String request_id,String checker_privilege){
		
		/*
		 * 04062022
		 * 
		 * To find config line given header id
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT REQUEST_CHECKER_ID, "+ 
				 "REQUEST_ID "+
				 "FROM web_sales_request_checker AS WRC " + 
				 "WHERE WRC.REQUEST_ID=:request_id "+
				 "AND WRC.CHECKED_STATUS='RQS' "+
				 "AND WRC.CURRENT_APPROVER=:checker_privilege "+
				 " ")
				.setParameter("request_id", request_id)
				.setParameter("checker_privilege", checker_privilege)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Id",request[0]);
			r.put("Request_ID",request[1]);
		
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> findSalesRequestCheckerLines(String request_id){
		
		/*
		 * 04062022
		 * 
		 * To find config line given header id
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT REQUEST_CHECKER_ID, "+ 
				 "REQUEST_ID, "+
				 "CHECKED_STATUS, "+
				 "LAST_ACTION "+
				 "FROM web_sales_request_checker AS WRC " + 
				 "WHERE WRC.REQUEST_ID=:request_id "+
				 " ")
				.setParameter("request_id", request_id)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Id",request[0]);
			r.put("Request_ID",request[1]);
			r.put("Status",request[2]);
			r.put("Last_Action",request[3]);
		
		    result.add(r);
		    
		}
		
	
		return result;
	
	}

	
	/*
	 * For future development
	 */
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> findServiceConfigHeader(String svc_code,String acct_id){
		
		/*
		 * 04062022
		 * 
		 * To find if service based header is available
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT HEADER_ID, "+ 
				 "ACCT_ID, "+
				 "SVC_CODE, "+
				 "BASED_ON, "+
				 "AMOUNT_BASED, "+
				 "MAX_PRIORITY, "+
				 "EXEC_IN_LAST_APPROVE "+
				 "FROM web_maker_checker_svc_config_header AS SCH " + 
				 "WHERE SCH.ACCT_ID=:acct_id "+
				 "AND SCH.SVC_CODE=:svc_code ")
				.setParameter("acct_id", acct_id)
				.setParameter("svc_code", svc_code)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Header_Id",request[0]);
			r.put("Account_Id",request[1]);
			r.put("Service_Code",request[2]);
			r.put("Based_On",request[3]);
			r.put("Amount_Based",request[4]);
			r.put("Max_Priority",request[5]);
			r.put("Exec_After_Last_Approve",request[6]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> findServiceConfigLine(String header_id,String rank){
		
		/*
		 * 04062022
		 * 
		 * To find config line given header id & rank number
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT HEADER_ID, "+ 
				 "LINE_ID, "+
				 "ROLE_NAME, "+
				 "USER_NAME, "+
				 "USER_ID, "+
				 "STAFF_CODE, "+
				 "MAX_AMOUNT, "+
				 "CURRENCY, "+
				 "PRIORITY "+
				 "FROM web_maker_checker_svc_config_line AS SCL " + 
				 "WHERE SCL.HEADER_ID=:header_id "+
				 "AND SCL.PRIORITY=:rank ")
				.setParameter("header_id", header_id)
				.setParameter("rank", rank)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Header_Id",request[0]);
			r.put("Line_Id",request[1]);
			r.put("Role_Name",request[2]);
			r.put("User_Name",request[3]);
			r.put("User_Id",request[4]);
			r.put("Staff_Code",request[5]);
			r.put("Max_Amount",request[6]);
			r.put("Currency",request[7]);
			r.put("Priority",request[8]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> findServiceConfigLines(String header_id){
		
		/*
		 * 04062022
		 * 
		 * To find config lines given header id
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT HEADER_ID, "+ 
				 "LINE_ID, "+
				 "ROLE_NAME, "+
				 "USER_NAME, "+
				 "USER_ID, "+
				 "STAFF_CODE, "+
				 "MAX_AMOUNT, "+
				 "CURRENCY, "+
				 "PRIORITY "+
				 "FROM web_maker_checker_svc_config_line AS SCL " + 
				 "WHERE SCL.HEADER_ID=:header_id "+
				 " ")
				.setParameter("header_id", header_id)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Header_Id",request[0]);
			r.put("Line_Id",request[1]);
			r.put("Role_Name",request[2]);
			r.put("User_Name",request[3]);
			r.put("User_Id",request[4]);
			r.put("Staff_Code",request[5]);
			r.put("Max_Amount",request[6]);
			r.put("Currency",request[7]);
			r.put("Priority",request[8]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> findAccountConfigHeader(String acct_id){
		
		/*
		 * 04062022
		 * 
		 * To find if account based header is available
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT HEADER_ID, "+ 
				 "ACCT_ID, "+
				 "BASED_ON, "+
				 "AMOUNT_BASED, "+
				 "MAX_PRIORITY, "+
				 "EXEC_IN_LAST_APPROVE "+
				 "FROM web_maker_checker_account_config_header AS SAH " + 
				 "WHERE SAH.ACCT_ID=:acct_id ")
				.setParameter("acct_id", acct_id)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Header_Id",request[0]);
			r.put("Account_Id",request[1]);
			r.put("Based_On",request[2]);
			r.put("Amount_Based",request[3]);
			r.put("Max_Priority",request[4]);
			r.put("Exec_After_Last_Approve",request[5]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> findAccountConfigLine(String header_id,String rank){
		
		/*
		 * 04062022
		 * 
		 * To find config line given header id & rank number
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT HEADER_ID, "+ 
				 "LINE_ID, "+
				 "ROLE_NAME, "+
				 "USER_NAME, "+
				 "USER_ID, "+
				 "STAFF_CODE, "+
				 "MAX_AMOUNT, "+
				 "CURRENCY, "+
				 "PRIORITY "+
				 "FROM web_maker_checker_account_config_line AS ACL " + 
				 "WHERE ACL.HEADER_ID=:header_id "+
				 "AND ACL.PRIORITY=:rank ")
				.setParameter("header_id", header_id)
				.setParameter("rank", rank)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Header_Id",request[0]);
			r.put("Line_Id",request[1]);
			r.put("Role_Name",request[2]);
			r.put("User_Name",request[3]);
			r.put("User_Id",request[4]);
			r.put("Staff_Code",request[5]);
			r.put("Max_Amount",request[6]);
			r.put("Currency",request[7]);
			r.put("Priority",request[8]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> findAccountConfigLines(String header_id){
		
		/*
		 * 04062022
		 * 
		 * To find config line given header id
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT HEADER_ID, "+ 
				 "LINE_ID, "+
				 "ROLE_NAME, "+
				 "USER_NAME, "+
				 "USER_ID, "+
				 "STAFF_CODE, "+
				 "MAX_AMOUNT, "+
				 "CURRENCY, "+
				 "PRIORITY "+
				 "FROM web_maker_checker_account_config_line AS ACL " + 
				 "WHERE ACL.HEADER_ID=:header_id "+
				 " ")
				.setParameter("header_id", header_id)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Header_Id",request[0]);
			r.put("Line_Id",request[1]);
			r.put("Role_Name",request[2]);
			r.put("User_Name",request[3]);
			r.put("User_Id",request[4]);
			r.put("Staff_Code",request[5]);
			r.put("Max_Amount",request[6]);
			r.put("Currency",request[7]);
			r.put("Priority",request[8]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	
	
	
}
