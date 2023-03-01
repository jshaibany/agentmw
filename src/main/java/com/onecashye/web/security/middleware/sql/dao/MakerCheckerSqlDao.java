package com.onecashye.web.security.middleware.sql.dao;
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
public class MakerCheckerSqlDao {

	Logger logger = LogManager.getLogger(MakerCheckerSqlDao.class);
	
	private final EntityManager entityManager;
	private final Environment env;
	
	
	@Autowired
	public MakerCheckerSqlDao(@Qualifier("middlewareSqlEM") EntityManager entityManager, Environment env) {
		
		this.entityManager=entityManager;
		this.env = env;
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public String insertSalesRequest(Map<String,Object> entity) {
		
		//Added 23042022
		
		try {
			
			entityManager.createNativeQuery(
				    "INSERT INTO "+env.getProperty("table.sales.request.details")+" (" + 
				    "	REQUEST_ID," + 
				    "	SERVICE_CODE," + 
				    "	CURRENCY," +
				    "	BULK_FLAG) VALUES (?,?,?,?)" )
					.setParameter(1, entity.get("Request_ID"))
					.setParameter(2, entity.get("Service_Code"))
					.setParameter(3, entity.get("Currency"))
					.setParameter(4, entity.get("Bulk_Flag"))
					.executeUpdate();
			
			String lastId= entityManager.createNativeQuery(
				    "SELECT REQUEST_ID FROM "+env.getProperty("table.sales.request.details")+" WHERE REQUEST_ID=:request_id" )
					.setParameter("request_id", entity.get("Request_ID"))
					.getSingleResult().toString();
			
			
			return lastId;
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return null;
		}
		
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public String insertBulkSalesRequest(Map<String,Object> entity) {
		
		//Added 23042022
		
		try {
			
			entityManager.createNativeQuery(
				    "INSERT INTO "+env.getProperty("table.sales.request.details")+" (" + 
				    "	REQUEST_ID," + 
				    "	SERVICE_CODE," + 
				    "	CURRENCY," +
				    "	BULK_FLAG," +
				    "	BULK_REF) VALUES (?,?,?,?,?)" )
					.setParameter(1, entity.get("Request_ID"))
					.setParameter(2, entity.get("Service_Code"))
					.setParameter(3, entity.get("Currency"))
					.setParameter(4, entity.get("Bulk_Flag"))
					.setParameter(5, entity.get("Bulk_Ref"))
					.executeUpdate();
			
			String lastId= entityManager.createNativeQuery(
				    "SELECT REQUEST_ID FROM "+env.getProperty("table.sales.request.details")+" WHERE REQUEST_ID=:request_id" )
					.setParameter("request_id", entity.get("Request_ID"))
					.getSingleResult().toString();
			
			
			return lastId;
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return null;
		}
		
	}

	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public String insertBusinessCashoutRequest(Map<String,Object> entity) {
		
		//Added 23042022
		try {
			
			entityManager.createNativeQuery(
				    "INSERT INTO "+env.getProperty("table.sales.request.details")+" (" + 
				    "	REQUEST_ID," + 
				    "	SERVICE_CODE," + 
				    "	AUTH_PERSON," +
				    "	AUTH_ID_NUMBER," +
				    "	AUTH_MOBILE," +
				    "	CURRENCY," +
				    "	BULK_FLAG) VALUES (?,?,?,?,?,?,?)" )
					.setParameter(1, entity.get("Request_ID"))
					.setParameter(2, entity.get("Service_Code"))
					.setParameter(3, entity.get("Requester_Remarks"))
					.setParameter(4, entity.get("Authorized_ID"))
					.setParameter(5, entity.get("Authorized_Mobile"))
					.setParameter(6, entity.get("Currency"))
					.setParameter(7, entity.get("Bulk_Flag"))
					.executeUpdate();
			
			String lastId= entityManager.createNativeQuery(
				    "SELECT REQUEST_ID FROM "+env.getProperty("table.sales.request.details")+" WHERE REQUEST_ID=:request_id" )
					.setParameter("request_id", entity.get("Request_ID"))
					.getSingleResult().toString();
			
			
			return lastId;
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return null;
		}

		
		
		
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public String insertBusinessCashoutRequest_BAK(Map<String,Object> entity) {
		
		//Added 23042022
		try {
			
			entityManager.createNativeQuery(
				    "INSERT INTO "+env.getProperty("table.sales.request.details")+" (" + 
				    "	REQUEST_ID," + 
				    "	SERVICE_CODE," + 
				    "	AUTH_PERSON," +
				    "	AUTH_ID_NUMBER," +
				    "	AUTH_MOBILE," +
				    "	CURRENCY," +
				    "	BULK_FLAG) VALUES (?,?,?,?,?,?,?)" )
					.setParameter(1, entity.get("Request_ID"))
					.setParameter(2, entity.get("Service_Code"))
					.setParameter(3, entity.get("Authorized_Person"))
					.setParameter(4, entity.get("Authorized_ID"))
					.setParameter(5, entity.get("Authorized_Mobile"))
					.setParameter(6, entity.get("Currency"))
					.setParameter(7, entity.get("Bulk_Flag"))
					.executeUpdate();
			
			String lastId= entityManager.createNativeQuery(
				    "SELECT REQUEST_ID FROM "+env.getProperty("table.sales.request.details")+" WHERE REQUEST_ID=:request_id" )
					.setParameter("request_id", entity.get("Request_ID"))
					.getSingleResult().toString();
			
			
			return lastId;
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return null;
		}

		
		
		
	}

	
	
	
	
	
	/*
	 * Multi-Checker Support part
	 */
	

	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer createFirstSalesRequestCheckerLine(Map<String,Object> entity) {
		
		/*
		 * 04062022
		 * 
		 * For multi checker business flow
		 * 
		 * Only to be used when a sales request is created for the first time
		 */
		
	Integer i =entityManager.createNativeQuery(
			    "INSERT INTO "+env.getProperty("table.sales.request.checker")+" (" + 
			    "	REQUEST_ID ," + 
			    "	CHECKER_ACCOUNT_ID ," + 
			    "	CHECKER_USER_ID ," + 
			    "	CHECKED_STATUS ," + 
			    "	CHECKER_REFERENCE ," + 
			    "	CHECKER_REMARK ," + 
			    "	CHECKER_PERIORITY ," + 
			    "	CURRENT_APPROVER ," + 
			    "	LAST_ACTION " + 
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
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer createSecondSalesRequestCheckerLine(Map<String,Object> entity) {
		
		/*
		 * 04062022
		 * 
		 * For multi checker business flow
		 * 
		 * Only to be used when a sales request is created for the first time
		 */
		
		Integer i =entityManager.createNativeQuery(
			    "INSERT INTO "+env.getProperty("table.sales.request.checker")+" (" + 
			    "	REQUEST_ID ," + 
			    "	CHECKER_ACCOUNT_ID ," + 
			    "	CHECKER_USER_ID ," + 
			    "	CHECKED_STATUS ," + 
			    "	CHECKER_REFERENCE ," + 
			    "	CHECKER_REMARK ," + 
			    "	CHECKER_PERIORITY ," + 
			    "	CURRENT_APPROVER ," + 
			    "	LAST_ACTION " + 
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
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer updateSalesRequestCheckerLine(Map<String,Object> entity) {
		
		Query q = entityManager.createNativeQuery(
			    "UPDATE "+env.getProperty("table.sales.request.checker")+" " + 
			    "SET LAST_ACTION= :checker_action ," + 
			    "	 CHECKER_USER_ID= :updated_by , " + 
			    "	 CHECKED_STATUS= :status , " +
			    "	 CHECKER_REMARK= :remarks , " +
			    "	 CHECKER_REFERENCE= :reference , " +
			    "	 CHECKED_DATE= :updated_on  " + 
			    "WHERE REQUEST_ID= :request_id " +
			    "AND CHECKED_STATUS IN ('RQS','Checked2') "+
			    "AND CURRENT_APPROVER= :checker_privilege ")
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
		 * To find approval line of a SR in normal cases (Not a BCashOut Trx)
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT REQUEST_CHECKER_ID, "+ 
				 "REQUEST_ID "+
				 "FROM "+env.getProperty("table.sales.request.checker")+" " + 
				 "WHERE REQUEST_ID=:request_id "+
				 "AND ( (CHECKED_STATUS='RQS' AND CURRENT_APPROVER=:checker_privilege ) ) "+
				 "")
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
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> findCashOutSalesRequestCheckerLine(String request_id,String checker_privilege){
		
		/*
		 * 14082022
		 * 
		 * To find CashOut Checker Line to support the case of Master Manager rejecting a SR after it is approved
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT REQUEST_CHECKER_ID, "+ 
				 "REQUEST_ID "+
				 "FROM "+env.getProperty("table.sales.request.checker")+" " + 
				 "WHERE REQUEST_ID=:request_id "+
				 "AND ( (CHECKED_STATUS='RQS' AND CURRENT_APPROVER=:checker_privilege )  "+
				 "OR (CHECKED_STATUS='Checked2' AND CURRENT_APPROVER=:checker_privilege AND LAST_ACTION='APPROVE' ) ) "+
				 "")
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
				 "FROM "+env.getProperty("table.sales.request.checker")+" " + 
				 "WHERE REQUEST_ID=:request_id "+
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
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> findSalesRequestCheckerLines(String request_id,String account_id,List<String> privileges){
		
		/*
		 * 30082022
		 * 
		 * A7A
		 * 
		 * Fix bug: finding Checker Lines should be accompanied with User Account ID
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT REQUEST_CHECKER_ID, "+ 
				 "REQUEST_ID, "+
				 "CHECKED_STATUS, "+
				 "LAST_ACTION, "+
				 "CURRENT_APPROVER "+
				 "FROM "+env.getProperty("table.sales.request.checker")+" " + 
				 "WHERE REQUEST_ID=:request_id "+
				 "AND CURRENT_APPROVER IN (:privileges) "+
				 "AND CHECKER_ACCOUNT_ID=:account_id ")
				.setParameter("request_id", request_id)
				.setParameter("account_id", account_id)
				.setParameter("privileges", privileges)
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
			r.put("Current_Approver",request[4]);
		
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer confirmSalesRequestAgentFlag(String request_id) {
		
		/*
		 * 30082022
		 * 
		 * Added to confirm AGENT view of Cashout Trx only
		 */
		Query q = entityManager.createNativeQuery(
			    "UPDATE "+env.getProperty("table.sales.request.checker")+" " + 
			    "SET CONFIRM_FLAG= 1 " + 
			  
			    "WHERE REQUEST_ID= :request_id " +
			    "AND CHECKED_STATUS IN ('Checked1','Checked2') ")
				.setParameter("request_id", request_id);
		
		Integer count=q.executeUpdate();
		
		return count;
		
		
	}
	
	/*
	 * 21082022
	 * 
	 * Below is part related to the test of changing the Cashout process (maker/checker) requested by Samir
	 */
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> findInternalCashOutSalesRequest(String request_id){
		
		/*
		 * 21082022
		 * 
		 * To find internal record in CASHOUT_SALES_REQUEST_DETAILS Table 
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT LOCAL_REQUEST_ID, "+ 
				 "REQUEST_ID, "+
				 "CHECKED_STATUS "+
				 "FROM "+env.getProperty("table.sales.request.checker.new")+" " + 
				 "WHERE REQUEST_ID=:request_id "+
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
		
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> findAndValidateInternalCashOutSalesRequest(String request_id,String current_approver,String account_id){
		
		/*
		 * 22082022
		 * 
		 * To find & valiadte internal record in CASHOUT_SALES_REQUEST_DETAILS Table 
		 * 
		 * If the record is in pending status (REQUEST_ID IS NULL) a result should be returned, otherwise return null
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT LOCAL_REQUEST_ID, "+ 
				 "REQUEST_ID, "+
				 "CHECKED_STATUS "+
				 "FROM "+env.getProperty("table.sales.request.checker.new")+" " + 
				 "WHERE LOCAL_REQUEST_ID=:request_id "+
				 "AND CURRENT_APPROVER=:current_approver "+
				 "AND SOURCE_ACCOUNT=:account_id "+
				 "AND REQUEST_ID IS NULL "+
				 " ")
				.setParameter("request_id", request_id)
				.setParameter("current_approver", current_approver)
				.setParameter("account_id", account_id)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Id",request[0]);
			r.put("Request_ID",request[1]);
			r.put("Status",request[2]);
		
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer rejectInternalCashOutRequestChecker1(String request_id,String user_id,String remarks,String checker_privilege) {
		
		Query q = entityManager.createNativeQuery(
			    "UPDATE "+env.getProperty("table.sales.request.checker.new")+" " + 
			    "SET CHECKED_STATUS='DCL' ," + 
			    "	 CHECKER1_USER_ID= :user_id , " + 
			    "	 CHECKER1_REMARK= :remarks , " +
			    "	 CHECKED1_STATUS='DCL' ,"+
			    "	 CHECKED1_DATE= :updated_on,  " +
			    "	 REQUEST_STATUS= 'DCL',  " +
			    "	 REQUEST_FINAL_STATUS= 'REJECTED'  " +
			    "WHERE LOCAL_REQUEST_ID= :request_id " +
			    "AND CHECKED_STATUS IN ('RQS') "+
			    "AND CURRENT_APPROVER= :checker_privilege ")
				.setParameter("request_id", request_id)
				.setParameter("user_id", user_id)
				.setParameter("remarks", remarks)	
				.setParameter("checker_privilege", checker_privilege)
				.setParameter("updated_on", Timestamp.valueOf(LocalDateTime.now()));
		
		Integer count=q.executeUpdate();
		
		return count;
		
		
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer approveInternalCashOutRequestChecker1(String request_id,String user_id,String remarks,String checker_privilege,String next_checker) {
		
		Query q = entityManager.createNativeQuery(
			    "UPDATE "+env.getProperty("table.sales.request.checker.new")+" " + 
			    "SET CHECKED_STATUS='RQS' ," + 
			    "	 REQUEST_STATUS='RQS' , " + 
			    "	 CHECKER1_USER_ID= :user_id , " + 
			    "	 CHECKER1_REMARK= :remarks , " +
			    "	 CHECKED1_STATUS='APR' ,"+
			    "	 CHECKED1_DATE= :updated_on , " + 
			    "	 CURRENT_APPROVER=:next_checker "+
			    "WHERE LOCAL_REQUEST_ID= :request_id " +
			    "AND CHECKED_STATUS IN ('RQS') "+
			    "AND CURRENT_APPROVER= :checker_privilege ")
				.setParameter("request_id", request_id)
				.setParameter("user_id", user_id)
				.setParameter("remarks", remarks)	
				.setParameter("checker_privilege", checker_privilege)
				.setParameter("next_checker", next_checker)
				.setParameter("updated_on", Timestamp.valueOf(LocalDateTime.now()));
		
		Integer count=q.executeUpdate();
		
		return count;
		
		
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer rejectInternalCashOutRequestChecker2(String request_id,String user_id,String remarks,String checker_privilege) {
		
		Query q = entityManager.createNativeQuery(
			    "UPDATE "+env.getProperty("table.sales.request.checker.new")+" " + 
			    "SET CHECKED_STATUS='DCL' ," + 
			    "	 CHECKER2_USER_ID= :user_id , " + 
			    "	 CHECKER2_REMARK= :remarks , " +
			    "	 CHECKED2_STATUS='DCL' ,"+
			    "	 CHECKED2_DATE= :updated_on , " + 
			    "	 REQUEST_STATUS= 'DCL',  " +
			    "	 REQUEST_FINAL_STATUS= 'REJECTED'  " +
			    "WHERE LOCAL_REQUEST_ID= :request_id " +
			    "AND CHECKED_STATUS IN ('RQS') "+
			    "AND CHECKED1_STATUS='APR' "+
			    "AND CURRENT_APPROVER= :checker_privilege ")
				.setParameter("request_id", request_id)
				.setParameter("user_id", user_id)
				.setParameter("remarks", remarks)	
				.setParameter("checker_privilege", checker_privilege)
				.setParameter("updated_on", Timestamp.valueOf(LocalDateTime.now()));
		
		Integer count=q.executeUpdate();
		
		return count;
		
		
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer preApprovalInternalCashOutRequestChecker2(String request_id) {
		
		//This is added 23082022 to change the internal request status into PROCESSING to indicate that request is one step before SR creation in Telepin
		
		Query q = entityManager.createNativeQuery(
			    "UPDATE "+env.getProperty("table.sales.request.checker.new")+" " + 
			    "SET CHECKED_STATUS='RQS' ," + 
			    "	 REQUEST_STATUS='RQS' , " + 
			    "	 CHECKED2_DATE= :updated_on , " + 
			    "	 REQUEST_FINAL_STATUS= 'PROCESSING'  " +
			    "WHERE LOCAL_REQUEST_ID= :request_id " +
			    "AND CHECKED_STATUS IN ('RQS') "+
			    " ")
				.setParameter("request_id", request_id)
				.setParameter("updated_on", Timestamp.valueOf(LocalDateTime.now()));
		
		Integer count=q.executeUpdate();
		
		return count;
		
		
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer approveInternalCashOutRequestChecker2(String request_id,String user_id,String remarks,String sales_request) {
		
		Query q = entityManager.createNativeQuery(
			    "UPDATE "+env.getProperty("table.sales.request.checker.new")+" " + 
			    "SET CHECKED_STATUS='RQS' ," + 
			    "	 REQUEST_STATUS='RQS' , " + 
			    "	 CHECKER2_USER_ID= :user_id , " + 
			    "	 CHECKER2_REMARK= :remarks , " +
			    "	 CHECKED2_STATUS='APR' ,"+
			    "	 CHECKED2_DATE= :updated_on , " + 
			    "	 REQUEST_ID=:sales_request , "+
			    "	 REQUEST_FINAL_STATUS= 'APPROVED'  " +
			    "WHERE LOCAL_REQUEST_ID= :request_id " +
			    "AND CHECKED_STATUS IN ('RQS') "+
			    " ")
				.setParameter("request_id", request_id)
				.setParameter("user_id", user_id)
				.setParameter("remarks", remarks)	
				.setParameter("sales_request", sales_request)
				.setParameter("updated_on", Timestamp.valueOf(LocalDateTime.now()));
		
		Integer count=q.executeUpdate();
		
		return count;
		
		
	}
	
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public Integer createInternalSalesRequestRecord(Map<String,Object> entity) {
		
		/*
		 * 21082022
		 * 
		 * For multi checker business flow
		 * 
		 * Only to be used with BCashOut when requested by a BCashOut Maker
		 */
		
	
		Integer i =entityManager.createNativeQuery(
			    "INSERT INTO "+env.getProperty("table.sales.request.checker.new")+" (" + 
			    "	MAKER_USER," + 
			    "	REQUEST_DATE," + 
			    "	SOURCE_ACCOUNT ," + 
			    "	SOURCE_MSISDN ," +
			    "	DEST_MSISDN ," + 
			    "	REQUEST_STATUS ," + 
			    "	BRAND_ID ," + 
			    "	ORIGINAL_AMOUNT ," + 
			    "	SO_AMOUNT ," + 
			    "	SOURCE_FEES1 ," + 
			    "	DEST_AMOUNT ," +
			    "	MAKER_MEMO ," +
			    "	MAKER_CHANNEL ," + 
			    "	PRINT_PERSON ," + 
			    "	PAYABLE_AMOUNT ," + 
			    "	TARGET_ALIAS ," +  
			    "	CHECKERS_REQUIRED ," +  
			    "	CURRENT_APPROVER, " +
			    "	SERVICE_CODE, " +
			    "	AUTH_PERSON, " +
			    "	AUTH_ID_NUMBER, " +
			    "	AUTH_MOBILE, " +
			    "	CURRENCY, " +
			    "	CHECKED_STATUS, " +
			    "	BULK_FLAG, " +
			    "	REQUEST_FINAL_STATUS " +
			    " ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" )
				.setParameter(1, entity.get("MAKER_USER"))
				.setParameter(2, LocalDateTime.now())
				.setParameter(3, entity.get("SOURCE_ACCOUNT"))
				.setParameter(4, entity.get("SOURCE_MSISDN"))
				.setParameter(5, entity.get("DEST_MSISDN"))
				.setParameter(6, "RQS") //REQUEST_STATUS
				.setParameter(7, entity.get("BRAND_ID"))
				.setParameter(8, entity.get("ORIGINAL_AMOUNT"))
				.setParameter(9, entity.get("SO_AMOUNT"))
				.setParameter(10, entity.get("SOURCE_FEES1"))
				.setParameter(11, entity.get("DEST_AMOUNT"))
				.setParameter(12, entity.get("MAKER_MEMO"))
				.setParameter(13, entity.get("MAKER_CHANNEL"))
				.setParameter(14, entity.get("PRINT_PERSON"))
				.setParameter(15, entity.get("PAYABLE_AMOUNT"))
				.setParameter(16, entity.get("TARGET_ALIAS"))
				.setParameter(17, 2)
				.setParameter(18, "WebChecker1")//Once an SR created the first checker is always Checker1
				.setParameter(19, "BZCASHOUT")
				.setParameter(20, entity.get("AUTH_PERSON"))
				.setParameter(21, entity.get("AUTH_ID_NUMBER"))
				.setParameter(22, entity.get("AUTH_MOBILE"))
				.setParameter(23, entity.get("CURRENCY"))
				.setParameter(24, "RQS")//CHECKED_STATUS
				.setParameter(25, "N")//Bulk Flag
				.setParameter(26, "PENDING")//REQUEST_FINAL_STATUS (PENDING, PROCESSING,APPROVED,REJECTED)

				.executeUpdate();
		
		return i;
		
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> findSalesRequestCheckerLine(String request_id){
		
		/*
		 * 17082022
		 * 
		 * To find SR Maker/Checker row
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT REQUEST_CHECKER_ID, "+ 
				 "REQUEST_ID, "+
				 "CHECKER_ACCOUNT_ID, "+
				 "MAKER_USER_ID, "+
				 "MAKER_DATE, "+
				 "CHECKED_STATUS, "+
				 "CHECKERS_REQUIRED, "+
				 "CURRENT_APPROVER "+
				 "FROM "+env.getProperty("table.sales.request.checker.new")+" " + 
				 "WHERE REQUEST_ID=:request_id "+
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
			r.put("Account_ID",request[2]);
			r.put("Maker_ID",request[3]);
			r.put("Creation_Date",request[4]);
			r.put("Status",request[5]);
			r.put("Checkers_Required",request[6]);
			r.put("Current_Approver",request[7]);
		
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> findInternalCashOutSalesRequestDetails(String request_id){
		
		/*
		 * 22082022
		 * 
		 * To find Internal CashOut Request details to be used in Telepin SR creation
		 */
		
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT SOURCE_MSISDN, "+ 
				 "DEST_MSISDN, "+
				 "BRAND_ID, "+
				 "ORIGINAL_AMOUNT, "+
				 "MAKER_MEMO, "+
				 "CURRENCY, "+
				 "TARGET_ALIAS, "+
				 "AUTH_PERSON, "+
				 "AUTH_ID_NUMBER, "+
				 "AUTH_MOBILE, "+
				 "SOURCE_FEES1, "+
				 "LOCAL_REQUEST_ID "+
				
				 "FROM "+env.getProperty("table.sales.request.checker.new")+" " + 
				 "WHERE LOCAL_REQUEST_ID=:request_id "+
				 " ")
				.setParameter("request_id", request_id)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		if(requests.size()<=0)
			return null;
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("SOURCE_MSISDN",request[0]);
			r.put("DEST_MSISDN",request[1]);
			r.put("BRAND_ID",request[2]);
			r.put("ORIGINAL_AMOUNT",request[3]);
			r.put("MAKER_MEMO",request[4]);
			r.put("CURRENCY",request[5]);
			r.put("TARGET_ALIAS",request[6]);
			r.put("AUTH_PERSON",request[7]);
			r.put("AUTH_ID_NUMBER",request[8]);
			r.put("AUTH_MOBILE",request[9]);
			r.put("SOURCE_FEES1",request[10]);
			r.put("LOCAL_REQUEST_ID",request[11]);
			
		
		    result.add(r);
		    
		}
		
	
		return result;
	
	}
	
	/*
	 * 05092022
	 * 
	 * 
	 */
	@Transactional(value="mwSqlTransactionManager",readOnly = false)
	public String insertCashOutAuditLog(Map<String,Object> entity) {
		
		//Added 05092022
		
		try {
			
			entityManager.createNativeQuery(
				    "INSERT INTO "+env.getProperty("table.cashout.audit.log")+" (" + 
				    "	SALES_ORDER_NUMBER," + 
				    "	USER_KEY," + 
				    "	MSISDN," +
				    "	ACTION" +
				    "	) VALUES (?,?,?,?)" )
					.setParameter(1, entity.get("Order_Number"))
					.setParameter(2, entity.get("User_Id"))
					.setParameter(3, entity.get("Msisdn"))
					.setParameter(4, entity.get("Action"))
					.executeUpdate();
			
			String lastId= entityManager.createNativeQuery(
				    "SELECT SALES_ORDER_NUMBER FROM "+env.getProperty("table.cashout.audit.log")+" WHERE SALES_ORDER_NUMBER=:order_number AND ACTION=:action " )
					.setParameter("order_number", entity.get("Order_Number"))
					.setParameter("action", entity.get("Action"))
					.getSingleResult().toString();
			
			if(lastId == null || lastId.isEmpty())
				throw new Exception();
			
			return lastId;
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return null;
		}
		
	}
	
}
