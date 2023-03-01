package com.onecashye.web.security.middleware.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.onecashye.web.security.exception.FailedToCreateSqlServerRecord;
import com.onecashye.web.security.middleware.dao.MakerCheckerDao;
import com.onecashye.web.security.middleware.sql.dao.MakerCheckerSqlDao;
import com.onecashye.web.security.telepin.dao.SalesRequestDao;

@Service
public class MakerCheckerService {

	Logger logger = LogManager.getLogger(MakerCheckerService.class);
	
	private final MakerCheckerDao makerCheckerDao;
	private final MakerCheckerSqlDao makerCheckerSqlDao;
	private final SalesRequestDao salesRequestDao;
	
	@Autowired
	public MakerCheckerService(MakerCheckerDao makerCheckerDao, MakerCheckerSqlDao makerCheckerSqlDao, SalesRequestDao salesRequestDao) {
		this.makerCheckerDao = makerCheckerDao;
		this.makerCheckerSqlDao = makerCheckerSqlDao;
		this.salesRequestDao = salesRequestDao;
		
	}
	
	@Deprecated
	public void createSalesRequest(Map<String,String> formattedResult) {
		
		Map<String,Object> entity = new HashMap<>(formattedResult);
		
		System.out.println(String.format("Maker Checker Record %s", makerCheckerDao.insertSalesRequest(entity)));
	}
	
	public String createSQLSalesRequest(Map<String,String> formattedResult) throws FailedToCreateSqlServerRecord {
		
		//Added 24042022
		
		Map<String,Object> entity = new HashMap<>(formattedResult);
		
		String result = makerCheckerSqlDao.insertSalesRequest(entity);
		
		System.out.println(String.format("Maker Checker Record %s",result ));
		
		if(result==null || result.isEmpty())
			throw new FailedToCreateSqlServerRecord(String.format("Sales Request %s is not created on SQL Server", entity.get("Request_ID")));
		
		return result;
	}
	
	@Deprecated
	public void createBulkSalesRequest(Map<String,String> formattedResult) {
		
		Map<String,Object> entity = new HashMap<>(formattedResult);
		
		System.out.println(String.format("Maker Checker Record %s", makerCheckerDao.insertBulkSalesRequest(entity)));
	}
	
	public String createSQLBulkSalesRequest(Map<String,String> formattedResult) throws FailedToCreateSqlServerRecord {
		
		//Added 24042022
		
		Map<String,Object> entity = new HashMap<>(formattedResult);
		
		String result = makerCheckerSqlDao.insertBulkSalesRequest(entity);
		
		System.out.println(String.format("Maker Checker Record %s",result ));
		
		if(result==null || result.isEmpty())
			throw new FailedToCreateSqlServerRecord(String.format("Sales Request %s is not created on SQL Server", entity.get("Request_ID")));
		
		return result;
	}
	
	@Deprecated
	public void createBusinessCashoutRequest(Map<String,String> formattedResult) {
		
		Map<String,Object> entity = new HashMap<>(formattedResult);
		
		System.out.println(String.format("Maker Checker Record %s", makerCheckerDao.insertBusinessCashoutRequest(entity)));
	}
	
	public String createSQLBusinessCashoutRequest(Map<String,String> formattedResult) throws FailedToCreateSqlServerRecord {
		
		//Added 24042022
		
		Map<String,Object> entity = new HashMap<>(formattedResult);
		
		String result = makerCheckerSqlDao.insertBusinessCashoutRequest(entity);
		
		System.out.println(String.format("Maker Checker Record %s",result ));
		
		if(result==null || result.isEmpty())
			throw new FailedToCreateSqlServerRecord(String.format("Sales Request %s is not created on SQL Server", entity.get("Request_ID")));
		
		return result;
	}
	
	@Deprecated
	public void approveSalesRequest(Map<String,String> formattedResult) {
		
		formattedResult.put("Checker_Action", "APPROVE");
		formattedResult.put("Status", "PST");
		
		Map<String,Object> entity = new HashMap<>(formattedResult);
		
		System.out.println(String.format("Maker Checker Updated: %s", makerCheckerDao.approveSalesRequest(entity)));
	}
	
	@Deprecated
	public void rejectSalesRequest(Map<String,String> formattedResult) {
		
		formattedResult.put("Checker_Action", "REJECT");
		formattedResult.put("Status", "DCL");
		
		Map<String,Object> entity = new HashMap<>(formattedResult);
		
		System.out.println(String.format("Maker Checker Updated: %s", makerCheckerDao.rejectSalesRequest(entity)));
	}
	
	public List<Map<String,String>> findSelfPendingRequests(String username,
			String source_id,
			Integer pageNumber,
			Integer pageSize,
			String dest,
			LocalDateTime from,
			LocalDateTime to,
			String status,
			String currency){
		
		logger.info("Start findSelfPendingRequests service ...");
		logger.info(String.format("Check query parameters ... ", ""));
		
		if(dest==null || dest.isEmpty())
			dest="%";
		else
			dest="%"+dest+"%";
		
		logger.info(String.format("Destination parameter=%s ... ", dest)); 
		
		//Added 12-03-2022
		if(status==null || status.isEmpty())
			status="%";
		else
			status="%"+status+"%";
		
		logger.info(String.format("Status parameter=%s ... ", status)); 
		
		//Added 12-03-2022
		if(currency==null || currency.isEmpty())
			currency="%";
		else
			currency="%"+currency+"%";
		
		logger.info(String.format("Currency parameter=%s ... ", currency)); 
		
		logger.info(String.format("From parameter=%s ... ", from)); 
		logger.info(String.format("To parameter=%s ... ", to)); 
		
		return salesRequestDao.findSelfPendingSalesRequests(username, 
				source_id,
				pageNumber,
				pageSize,
				dest,
				from,
				to,
				status,
				currency);
	}
	
	public List<Map<String,String>> findPendingRequests(List<String> privileges,
			String source_id,
			Integer pageNumber,
			Integer pageSize,
			String dest,
			LocalDateTime from,
			LocalDateTime to,
			String status,
			String currency,
			String username){
		
		logger.info("Start findPendingRequests service ...");
		logger.info(String.format("Check query parameters ... ", ""));
		
		if(dest==null || dest.isEmpty())
			dest="%";
		else
			dest="%"+dest+"%";
		
		logger.info(String.format("Destination parameter=%s ... ", dest)); 
		
		//Added 12-03-2022
		if(status==null || status.isEmpty())
			status="%";
		else
			status="%"+status+"%";
		
		logger.info(String.format("Status parameter=%s ... ", status)); 
		
		//Added 12-03-2022
		if(currency==null || currency.isEmpty())
			currency="%";
		else
			currency="%"+currency+"%";
		
		logger.info(String.format("Currency parameter=%s ... ", currency)); 
		
		logger.info(String.format("From parameter=%s ... ", from)); 
		logger.info(String.format("To parameter=%s ... ", to)); 
		
		return salesRequestDao.findPendingSalesRequests(privileges, 
				source_id,
				pageNumber,
				pageSize,
				dest,
				from,
				to,
				status,
				currency,
				username);
	}
	
	public List<Map<String,String>> findBulkRelatedSalesRequests(List<String> privileges,
			String source_id,
			Integer pageNumber,
			Integer pageSize,
			String bulkReference,
			String username){
		
		logger.info("Start findBulkRelatedRequests service ...");
		logger.info(String.format("Check query parameters ... ", ""));
		
		
		return salesRequestDao.findBulkRelatedSalesRequests(privileges, 
				source_id,
				pageNumber,
				pageSize,
				bulkReference,
				username);
	}
	
	public Integer countPendingRequests(String username, List<String> privileges,
			String source_id,
			String dest,
			LocalDateTime from,
			LocalDateTime to,
			String status,
			String currency){
		
		if(dest==null || dest.isEmpty())
			dest="%";
		else
			dest="%"+dest+"%";
		
		//Added 12-03-2022
		if(status==null || status.isEmpty())
			status="%";
		else
			status="%"+status+"%";
				
		//Added 12-03-2022
		if(currency==null || currency.isEmpty())
			currency="%";
		else
			currency="%"+currency+"%";
		
		return salesRequestDao.countPendingSalesRequests(username, privileges, 
				source_id,
				dest,
				from,
				to,
				status,
				currency);
	}
	
	public Integer countBulkRelatedRequests(List<String> privileges,
			String source_id,
			String bulk_ref,
			String status){
		
		//Added 12-03-2022
		if(status==null || status.isEmpty())
			status="%";
		else
			status="%"+status+"%";
		/*
		return salesRequestDao.countBulkRelatedSalesRequests(privileges, 
				source_id,
				bulk_ref,
				status);*/
		
		return salesRequestDao.countBulkRelatedSalesRequests(source_id,
				bulk_ref);
	}
	
	public Optional<List<Map<String,String>>> findSalesRequest(String request_id,String username){
		
		List<Map<String,String>> r = salesRequestDao.findSalesRequest(request_id,username);
		
		if(r.size()<=0)
			return Optional.ofNullable(null);
		
		return Optional.ofNullable(r);
	}

	public Integer confirmSalesRequestAgentFlag(String request_id) {
		
		return makerCheckerSqlDao.confirmSalesRequestAgentFlag(request_id);
	}
	/*
	 * Multi-Checker functions
	 */
	
	public Optional<Map<String,Object>> createFirstChecker(Map<String,String> request){
		
		return createFirstCheckerLine(request);
	}
	
	/*
	public Optional<Map<String,Object>> createSecondChecker(Map<String,String> request){
		
		createSecondCheckerLine(request);
			
		return Optional.ofNullable(null);
	}*/
	
	public Optional<Map<String,Object>> createFirstChecker_Backup(Map<String,String> request){
		
		/*
		 * 1- Check for Service Config Header, Then Account Config Header, Otherwise go as default
		 */
		
		List<Map<String,Object>> header = makerCheckerDao
				.findServiceConfigHeader(request.get("Service_Code"), 
				request.get("Source_Id"));
		
		if(header!=null) {
			
			//Call service to complete the task
			createServiceBasedChecker(header.get(0),request);
		}
		else {
			
			header = makerCheckerDao.findAccountConfigHeader(request.get("Source_Id"));
			
			if(header!=null) {
				
				//Call service to complete the task
				createAccountBasedChecker(header.get(0),request);
			}
			else {
				
				//Call default (GROUP) based 
				createFirstCheckerLine(request);
			}
		}
			
		
		return Optional.ofNullable(null);
	}
	
	private Optional<Map<String,Object>> createServiceBasedChecker(Map<String,Object> header,Map<String,String> request){
		
		Map<String,Object> entity = new HashMap<>();
		
		entity.put("Request_ID", request.get("Request_ID"));
		entity.put("Entity", "SERVICE");
		entity.put("Based_On", header.get("Based_On"));
		entity.put("Header_Id", header.get("Header_Id"));
		entity.put("Amount_Based", header.get("Amount_Based"));
		entity.put("Exec_In_Last_Action", header.get("Exec_After_Last_Approve"));
		entity.put("Svc_Code", request.get("Service_Code"));
		
		List<Map<String,Object>> line = makerCheckerDao
				.findServiceConfigLine(header.get("Header_Id").toString(), "1");
		
		if(line!=null) {
			
			entity.put("Amount", line.get(0).get("Max_Amount"));
			entity.put("Currency", line.get(0).get("Currency"));
			
			String b = (String) header.get("Based_On");
			
			if(b.contentEquals("ROLE"))
				entity.put("Current_Approver","WebChecker1");
			else
				entity.put("Current_Approver",line.get(0).get("User_Name"));
		}
		
		//makerCheckerDao.createSalesRequestCheckerLine(entity);
		
		return Optional.ofNullable(null);
	}
	
	private Optional<Map<String,Object>> createAccountBasedChecker(Map<String,Object> header,Map<String,String> request){
		
		Map<String,Object> entity = new HashMap<>();
		
		entity.put("Request_ID", request.get("Request_ID"));
		entity.put("Entity", "ACCOUNT");
		entity.put("Based_On", header.get("Based_On"));
		entity.put("Header_Id", header.get("Header_Id"));
		entity.put("Amount_Based", header.get("Amount_Based"));
		entity.put("Exec_In_Last_Action", header.get("Exec_After_Last_Approve"));
		entity.put("Svc_Code", request.get("Service_Code"));
		
		List<Map<String,Object>> line = makerCheckerDao
				.findAccountConfigLine(header.get("Header_Id").toString(), "1");
		
		if(line!=null) {
			
			entity.put("Amount", line.get(0).get("Max_Amount"));
			entity.put("Currency", line.get(0).get("Currency"));
			
			String b = (String) header.get("Based_On");
			
			if(b.contentEquals("ROLE"))
				entity.put("Current_Approver","WebChecker1");
			else
				entity.put("Current_Approver",line.get(0).get("User_Name"));
		}
		
		//makerCheckerDao.createSalesRequestCheckerLine(entity);
		
		return Optional.ofNullable(null);
	}
	
	private Optional<Map<String,Object>> createFirstCheckerLine(Map<String,String> request){
		
		Map<String,Object> result = new HashMap<>();
		
		logger.info(String.format("First checker record for Request ID=%s and Account ID=%s ..", request.get("Request_ID")
				,request.get("account_id")));
		
		Map<String,Object> entity = new HashMap<>();
		
		entity.put("Request_ID", request.get("Request_ID"));
		entity.put("Account_Id", request.get("account_id"));
		
		Integer i =makerCheckerSqlDao.createFirstSalesRequestCheckerLine(entity);
		
		result.put("created_rows", i);
		
		if(i<=0)
			logger.warn(String.format("Failed to create first checker record for Request ID=%s and Account ID=%s ..", request.get("Request_ID")
				,request.get("account_id")));
		else
			logger.info(String.format("First checker record for Request ID=%s and Account ID=%s is created successfully ..", request.get("Request_ID")
				,request.get("account_id")));
		
		return Optional.ofNullable(result);
	}
	
	/*
	private Optional<Map<String,Object>> createSecondCheckerLine(Map<String,String> request){
		
		logger.info(String.format("Second checker record for Request ID=% and Account ID=%s ..", request.get("Request_ID"))
				,request.get("account_id"));
		
		Map<String,Object> entity = new HashMap<>();
		
		entity.put("Request_ID", request.get("Request_ID"));
		entity.put("Account_Id", request.get("account_id"));
		
		
		Integer i=makerCheckerDao.createSecondSalesRequestCheckerLine(entity);
		
		if(i<=0)
			logger.warn(String.format("Failed to create second checker record for Request ID=% and Account ID=%s ..", request.get("Request_ID"))
					,request.get("account_id"));
		else
			logger.info(String.format("Second checker record for Request ID=% and Account ID=%s is created successfully ..", request.get("Request_ID"))
					,request.get("account_id"));
		
		return Optional.ofNullable(null);
	}*/

	public Optional<Map<String,Object>> rejectRequest(Map<String,Object> request){
		
		/*
		 * The reject logic is:
		 * - We assume that line 1 is created when SR is created with Status=RQS
		 * - We Update the DB table line which Status=RQS and SR=input SR
		 * - No second line is required in case of rejection
		 */
		
		Map<String,Object> result= new HashMap<>();
		
		logger.info(String.format("Reject Request= %s ,Account ID=%s, User ID=%s ..", request.get("request_id")
				,request.get("account_id")
				,request.get("user_id")));
		
		Integer i=makerCheckerSqlDao.updateSalesRequestCheckerLine(request);
		
		result.put("affected_rows", i);
		
		if(i<=0)
			logger.warn(String.format("Reject process has failed Request ID=%s and Account ID=%s ..", request.get("request_id")
					,request.get("account_id")));
		else
			logger.info(String.format("Reject Request ID=%s and Account ID=%s is done successfully ..", request.get("request_id")
					,request.get("account_id")));
			
		return Optional.ofNullable(result);
	}
	
	public Optional<Map<String,Object>> approveRequest(Map<String,Object> request){
		
		/*
		 * The approve logic is:
		 * - We assume that line 1 is created when SR is created with Status=RQS
		 * - We Update the DB table line which Status=RQS and SR=input SR
		 * - If current user is Checker1 then Update the first checker line, Then create the second line checker
		 */
		
		Map<String,Object> result= new HashMap<>();
		
		logger.info(String.format("Approve Request= %s ,Account ID=%s, User ID=%s ..", request.get("request_id")
				,request.get("account_id")
				,request.get("user_id")));
		
		Integer i=makerCheckerSqlDao.updateSalesRequestCheckerLine(request);
		
		result.put("affected_rows", i);
		
		if(i<=0)
			logger.warn(String.format("Approve process has failed Request ID=%s and Account ID=%s ..", request.get("request_id")
					,request.get("account_id")));
		else
			logger.info(String.format("Approve Request ID=%s and Account ID=%s is done successfully ..", request.get("request_id")
					,request.get("account_id")));
		
		if(i==1) {
			
			logger.info(String.format("Second checker record for Request ID=%s and Account ID=%s to be created ..", request.get("request_id")
					,request.get("account_id")));
			
			if(request.get("status").toString().contentEquals("Checked1"))
				result.put("second_row", makerCheckerSqlDao.createSecondSalesRequestCheckerLine(request));
			
		}
			
		return Optional.ofNullable(result);
	}

	public boolean isValidChecker(String request_id,List<String> privileges,String account_id) {
		
		List<Map<String,Object>> v = makerCheckerSqlDao.findSalesRequestCheckerLines(request_id,account_id, privileges);
		
		return (v!=null && v.size()>0);
		
	}
	
	public boolean isValidCashOutChecker(String request_id,String privilege) {
		
		/*
		 * Added 14082022 to support the new logic for SR approve/reject flow
		 */
		List<Map<String,Object>> v = makerCheckerSqlDao.findCashOutSalesRequestCheckerLine(request_id, privilege);
		
		return (v!=null && v.size()>0);
		
	}
	
	public boolean isValidCashoutToExecute(String request_id) {
		
		/*
		 * This function is used to validate CashOut sales requests if are ready to be executed by external agents
		 */
		List<Map<String,Object>> v = makerCheckerSqlDao.findSalesRequestCheckerLines(request_id);
		
		//Request not found or only approved once
		if(v==null || v.size()<2)
			return false;
		
		String status1 = v.get(0).get("Status").toString();
		String status2 = v.get(1).get("Status").toString();
		
		String action1 = v.get(0).get("Last_Action").toString();
		String action2 = v.get(1).get("Last_Action").toString();
		
		//Request is valid
		if(status1.contentEquals("Checked1") && status2.contentEquals("Checked2")
				&& action1.contentEquals("APPROVE") && action2.contentEquals("APPROVE"))
			return true;
		
		//Otherwise it is invalid
		return false;
		
	}
	
	
	/*
	 * New Multi Checkers
	 */
	
	public Optional<List<Map<String,Object>>> findSalesRequestCheckersLine(String request_id){
		
		List<Map<String,Object>> r = makerCheckerSqlDao.findSalesRequestCheckerLines(request_id);
		
		if(r.size()<=0)
			return Optional.ofNullable(null);
		
		return Optional.ofNullable(r);
	}
	
	public Optional<List<Map<String,Object>>> findSalesRequestCheckersLine(String request_id,String account_id,List<String> privileges){
		
		/*
		 * 30082022
		 * 
		 * A7A
		 * 
		 * Add user account ID
		 * 
		 * This function to replace findSalesRequestCheckersLine(String request_id)
		 */
		List<Map<String,Object>> r = makerCheckerSqlDao.findSalesRequestCheckerLines(request_id,account_id,privileges);
		
		if(r==null || r.size()<=0)
			return Optional.ofNullable(null);
		
		return Optional.ofNullable(r);
	}

	public Optional<Map<String,Object>> createInternalCashOutRequest(Map<String,String> tcsResult,Map<String,Object> jsonRequest){
		
		//Added 21082022
		
		//Prepare the cashOutRequestEntity
		
		Map<String,Object> cashOutRequestEntity = new HashMap<>();
		
		cashOutRequestEntity.put("SOURCE_ACCOUNT", tcsResult.get("account_id"));
		cashOutRequestEntity.put("SOURCE_MSISDN", jsonRequest.get("source").toString());
		cashOutRequestEntity.put("MAKER_USER", tcsResult.get("user_id"));
		if(jsonRequest.get("destination").toString().length()<=6)
			cashOutRequestEntity.put("TARGET_ALIAS", jsonRequest.get("destination").toString());
		cashOutRequestEntity.put("DEST_MSISDN", tcsResult.get("Target_MSISDN"));
		cashOutRequestEntity.put("BRAND_ID", jsonRequest.get("brand").toString());
		cashOutRequestEntity.put("ORIGINAL_AMOUNT", jsonRequest.get("amount").toString());
		cashOutRequestEntity.put("SO_AMOUNT", 0.0);// ORIGINAL_AMOUNT + SOURCE_FEES1
		cashOutRequestEntity.put("SOURCE_FEES1", tcsResult.get("Total_Fees"));
		cashOutRequestEntity.put("DEST_AMOUNT", tcsResult.get("Payment_Amount"));
		
		String delegate_data = String.format("%s#%s#%s", 
				jsonRequest.get("authorized_person").toString(),
				jsonRequest.get("authorized_id").toString(),
				jsonRequest.get("authorized_mobile").toString());
		
		cashOutRequestEntity.put("MAKER_MEMO", delegate_data);
		
		//String remarks = (String) jsonRequest.get("remarks");
		
		cashOutRequestEntity.put("AUTH_PERSON", jsonRequest.get("authorized_person").toString());
		
		cashOutRequestEntity.put("PRINT_PERSON", jsonRequest.get("username").toString());
		cashOutRequestEntity.put("PAYABLE_AMOUNT", tcsResult.get("Payment_Amount"));
		cashOutRequestEntity.put("AUTH_ID_NUMBER", jsonRequest.get("authorized_id").toString());
		cashOutRequestEntity.put("AUTH_MOBILE", jsonRequest.get("authorized_mobile").toString());
		cashOutRequestEntity.put("CURRENCY", jsonRequest.get("currency").toString());
		
		Map<String,Object> result = new HashMap<>();
		
		logger.info(String.format("A checker record for Request ID=%s and Account ID=%s ..", tcsResult.get("Request_ID")
				,tcsResult.get("account_id")));
		
		
		//Try to insert record
		
		Integer i =makerCheckerSqlDao.createInternalSalesRequestRecord(cashOutRequestEntity);
		
		result.put("created_rows", i);
		
		if(i<=0) {
			
			
			logger.error(String.format("Failed to create a checker record for .."));
			return Optional.ofNullable(null);
		}
			
		
	    logger.info(String.format("Internal SR is created successfully .."));
		
		return Optional.ofNullable(result);
	}
	
	public boolean isValidCashoutToExecute2(String request_id) {
		
		/*
		 * Added 21082022
		 * 
		 * This function is used to validate CashOut sales request if is complete in order to be executed by external agents
		 */
		List<Map<String,Object>> v = makerCheckerSqlDao.findInternalCashOutSalesRequest(request_id);
		
		//Request not found/ Invalid
		if(v==null)
			return false;
		
		return true;
		
	}

	public boolean isValidCashOutForApproval(String request_id,String account_id,List<String> privileges) {
		
		//22082022
		//This function should check if the provided request_id is an internal cash out record and if the requester is the source account
		
		List<Map<String,Object>> result = makerCheckerSqlDao.findAndValidateInternalCashOutSalesRequest(request_id, privileges.get(0), account_id);
		
		if(result == null || result.size()<=0)
		return false;
		
		return true;
	}

	public Integer rejectInternalCashOutRequestChecker1(String request_id,String user_id,String remarks,String checker_privilege) {
		
		return makerCheckerSqlDao.rejectInternalCashOutRequestChecker1(request_id, user_id, remarks, checker_privilege);
	}
	
	public Integer approveInternalCashOutRequestChecker1(String request_id,String user_id,String remarks,String checker_privilege, String next_checker) {
		
		return makerCheckerSqlDao.approveInternalCashOutRequestChecker1(request_id, user_id, remarks, checker_privilege, next_checker);
	}
	
	public Integer rejectInternalCashOutRequestChecker2(String request_id,String user_id,String remarks,String checker_privilege) {
		
		return makerCheckerSqlDao.rejectInternalCashOutRequestChecker2(request_id, user_id, remarks, checker_privilege);
	}
	
	public Integer preApprovalInternalCashOutRequestChecker2(String request_id) {
		
		return makerCheckerSqlDao.preApprovalInternalCashOutRequestChecker2(request_id);
	}

	public Integer approveInternalCashOutRequestChecker2(String request_id,String user_id,String remarks, String sales_request) {
		
		return makerCheckerSqlDao.approveInternalCashOutRequestChecker2(request_id, user_id, remarks, sales_request);
	}
	
	public Optional<Map<String,Object>> findInternalCashOutSalesRequestDetails(String request_id){
		
		List<Map<String,Object>> result = makerCheckerSqlDao.findInternalCashOutSalesRequestDetails(request_id);
		
		if(result != null && result.size()==1)
			return Optional.ofNullable(result.get(0));
		
		return null;
	}
	
	public Map<String,Object> buildCashOutSalesRequest(Map<String,Object> local_request,Map<String,Object> jsonRequest,String user_name,String account_id){
		
		Map<String,Object> jr = new HashMap<>();
		
		
		jr.put("brand", local_request.get("BRAND_ID"));
		jr.put("source", local_request.get("SOURCE_MSISDN"));
		jr.put("source_id", account_id);
		jr.put("user_id", jsonRequest.get("user_id"));
		
		if(local_request.get("TARGET_ALIAS")==null)
			jr.put("destination", local_request.get("DEST_MSISDN"));
		else
			jr.put("destination", local_request.get("TARGET_ALIAS"));
		
		jr.put("username",user_name);
		jr.put("password", jsonRequest.get("password"));
		jr.put("auth_person",local_request.get("AUTH_PERSON"));
		jr.put("auth_id",local_request.get("AUTH_ID_NUMBER"));
		jr.put("auth_mobile",local_request.get("AUTH_MOBILE"));
		jr.put("amount", local_request.get("ORIGINAL_AMOUNT"));
		jr.put("local_request_id", jsonRequest.get("request_id"));
		jr.put("remarks", local_request.get("MAKER_MEMO"));
		jr.put("currency", local_request.get("CURRENCY"));
		jr.put("maker_reference", jsonRequest.get("request_id"));
		jr.put("check", "false");
		
		return jr;
	}

	public boolean isSourceOrDestination(Map<String,String> salesRequest, String user_msisdn){
		
		/*
		 * Added 24082022 To check if user account is source or destination of a Telepin SR
		 */
		
		
		String dest_msisdn = salesRequest.get("Destination_Account");
		String source_msisdn = salesRequest.get("Source_Account");
		
		logger.info(String.format("Sales Request Destination MSISDN %s", dest_msisdn));
		
		
		/*
		 * Wrong conditions:
		 * 1- If user to action is not the destination
		 * 2- If user to action is the source and action is APPROVE
		 */

		if(!source_msisdn.contentEquals(user_msisdn)) {
			
			if(!dest_msisdn.contentEquals(user_msisdn)) {
				
				return false;
			}
		}
		
		return true;
	}
	
	public Optional<String> insertCashOutAuditLog(Map<String,Object> entity){
		
		/*
		 * 05092022
		 * 
		 * Added to audit cashout trx
		 */
		String r=makerCheckerSqlDao.insertCashOutAuditLog(entity);
		
	    return Optional.ofNullable(r);
	}
}
