package com.onecashye.web.security.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;

import com.onecashye.web.security.helper.MappingParamsHelper;
import com.onecashye.web.security.helper.MatcherHelper;
import com.onecashye.web.security.middleware.service.MakerCheckerService;
import com.onecashye.web.security.middleware.sql.dao.ForexSqlDao;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

/**
 * Used to connect to TCS to send requests using {@code httpClient} ,and also
 * Used to support Maker/Checker logic using {@code makerCheckerService}
 * 
 * @author JalalAlShaibani
 *
 */
@Service
public class TcsApiPaymentService {

	Logger logger = LogManager.getLogger(TcsApiPaymentService.class);
	
	private final TcsServiceInitializer tcsServiceInitializer;
	private HttpClient httpClient;
	private final MakerCheckerService makerCheckerService;
	private final ForexSqlDao forexSqlDao;
	
	@Autowired
	public TcsApiPaymentService(TcsServiceInitializer tcsServiceInitializer,
			JwtSecurityService securityService,
			MakerCheckerService makerCheckerService,
			ForexSqlDao forexDao) {
		super();
		this.tcsServiceInitializer=tcsServiceInitializer;
		this.makerCheckerService = makerCheckerService;
		this.forexSqlDao = forexDao;
		
		httpClient = HttpClient.create()
				  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, tcsServiceInitializer.getTcsCallTimeout())
				  .responseTimeout(Duration.ofMillis(tcsServiceInitializer.getTcsCallTimeout()))
				  .doOnConnected(conn -> 
				    conn.addHandlerLast(new ReadTimeoutHandler(30))
				      .addHandlerLast(new WriteTimeoutHandler(tcsServiceInitializer.getTcsCallTimeout(), TimeUnit.MILLISECONDS)));
	}
	
	public Map<String,String> tcsCashPaymentCheck(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		/*
		 * For Customer Cash In, Business Cash In and Out
		 */

		try {
			
			logger.info("Starting tcsCashPaymentCheck service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapCashPaymentCheck(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsCashPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * For Customer Cash In, Business Cash In
		 */
		

		try {
			
			logger.info("Starting tcsCashPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapCashPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	

	public Map<String,String> tcsBusinessCashOutPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		/*
		 * 06092022
		 * To be used for Cashout single shot payment
		 * 
		 * Because of the special requirement of Delegate data to be included in extra info param
		 */

		try {
			
			logger.info("Starting tcsBusinessCashOutPayment service ..");
			
			String delegate_data = String.format("%s#%s#%s", 
					jsonRequest.get("authorized_person").toString(),
					jsonRequest.get("authorized_id").toString(),
					jsonRequest.get("authorized_mobile").toString());
			
			String note = "";
			
			if(jsonRequest.get("extra") != null)
				note = String.format("%s", jsonRequest.get("extra"));
			
			jsonRequest.remove("extra");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			/*
			 * Always replace <param7>{extra}</param7> in XML body with <param7>note</param7><param8>note</param8><param14>delegate_data</param14>
			 */
			logger.info(String.format("Replace extra input with delegate data and put in param8 & param7"));
			
			replace = "<param7>{extra}</param7>";
			String substitute=String.format("<param7>%s</param7><param8>%s</param8><param12>%s</param12>", note,note,delegate_data);
			
			xml = xml.replace(replace, substitute);
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapCashPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	
	public Map<String,String> tcsBusinessOnbehalfPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		/*
		 * 05022023
		 * To be used for Business onbehalf transfers
		 * 
		 */

		try {
			
			logger.info("Starting tcsBusinessOnbehalfPayment service ..");
			
			String transfer_data = String.format("%s#%s", 
					jsonRequest.get("transfer_source").toString(),
					jsonRequest.get("transfer_number").toString());
			
			String note = "";
			
			
			if(jsonRequest.get("extra") != null)
				note = String.format("%s", jsonRequest.get("extra"));
			
			
			jsonRequest.remove("extra");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			/*
			 * Always replace <param7>{extra}</param7> in XML body with <param7>note</param7><param8>note</param8><param14>delegate_data</param14>
			 */
			logger.info(String.format("Replace extra input with transfer data and put in param8 & param7"));
			
			replace = "<param7>{extra}</param7>";
			String substitute=String.format("<param7>%s</param7><param8>%s</param8><param12>%s</param12>", note,note,transfer_data);
			
			xml = xml.replace(replace, substitute);
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapCashPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	

	public Map<String,String> tcsForexPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		/*
		 * 18092022
		 * To be used for Forex single shot payment
		 * 
		 * 
		 */

		try {
			
			logger.info("Starting tcsBusinessCashOutPayment service ..");
			
			String conversion_rate = String.format("%s", jsonRequest.get("conversion_rate").toString());
			String converted_amount = String.format("%s", jsonRequest.get("converted_amount").toString());
			String bank_account = String.format("%s", jsonRequest.get("bank_account").toString());
			
			logger.info(String.format("User data entry for forex transaction Rate=%s ,Amount=%s, Bank=%s", conversion_rate,converted_amount,bank_account));
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTCSFunctionForex());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapCashPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	
	public Map<String,String> tcsSellFloatCheck(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		/*
		 * 
		 */

		try {
			
			logger.info("Starting tcsSellFloatCheck service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSellFloatCheck(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsDeleteMoneyCheck(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		/*
		 * 22102022
		 */

		try {
			
			logger.info("Starting tcsDeleteMoneyCheck service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
			
			replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
			
			xml = xml.replace(replace, "");
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapDeleteMoneyCheck(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsSellFloat(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * 
		 */
		

		try {
			
			logger.info("Starting tcsSellFloat service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapSellFloat(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsDeleteMoney(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * 22102022
		 */
		

		try {
			
			logger.info("Starting tcsDeleteMoney service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
			
			replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
			
			xml = xml.replace(replace, "");
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapDeleteMoney(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsPushFloatCheck(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		/*
		 * 
		 */

		try {
			
			logger.info("Starting tcsPushFloatCheck service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapPushFloatCheck(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsPushFloat(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * 
		 */
		

		try {
			
			logger.info("Starting tcsPushFloat service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapPushFloat(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsPullFloatCheck(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * PROXYTRANSACTION CheckOnly=true
		 */
		

		try {
			
			logger.info("Starting tcsPullFloatCheck service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionProxyTransaction());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("destination").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param2>%s</param2>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra1") == null || jsonRequest.get("extra1").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra1}</param7>";
				
				xml = xml.replace(replace, " ");
				
				
			}
			
			if(jsonRequest.get("extra2") == null || jsonRequest.get("extra2").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param8>{extra2}</param8>";
				
				xml = xml.replace(replace, " ");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapPullFloatCheck(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsPullFloat(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * PROXYTRANSACTION CheckOnly=false
		 */
		

		try {
			
			logger.info("Starting tcsPullFloat service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionProxyTransaction());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("destination").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param2>%s</param2>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra1") == null || jsonRequest.get("extra1").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra1}</param7>";
				
				xml = xml.replace(replace, " ");
				
				
			}
			
			if(jsonRequest.get("extra2") == null || jsonRequest.get("extra2").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param8>{extra2}</param8>";
				
				xml = xml.replace(replace, " ");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapPullFloat(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsBillCheckPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		try {
			
			logger.info("Starting tcsBillCheckPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionBillPay());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapBillCheckPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsBillPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionBillPay());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			logger.info("Starting tcsBillPayment service ..");
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapBillPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsBillPaymentDevMode(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		
		try {
			
			return MappingParamsHelper.mapDummyBillPayment();
			
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsCheckBillPaymentDevMode(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		
		try {
			
			return MappingParamsHelper.mapDummyCheckBillPayment();
			
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsW2BACheckPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		try {
			
			logger.info("Starting tcsW2BACheckPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSendToBank());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapCheckBankPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsW2BAPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {


		try {
			
			logger.info("Starting tcsW2BAPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSendToBank());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapBankPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsRedeemCheckPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		try {
			
			logger.info("Starting tcsRedeemCheckPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionRedeem());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapRedeemCheckPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsRedeemPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		try {
			
			logger.info("Starting tcsRedeemPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionRedeem());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapRedeemPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsCheckVoucherPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		try {
			
			logger.info("Starting tcsCheckVoucherPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSendVoucher());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param14>{extra}</param14>";
				
				xml = xml.replace(replace, "");
				
			}
			
			if(jsonRequest.get("zone_id") == null || jsonRequest.get("zone_id").toString().isEmpty()) {
				
				replace = "";
				
				logger.info(String.format("No zone_id data is provided"));
				
				replace = "<param12>{zone_id}</param12>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapCheckVoucherPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsVoucherPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		
		try {
			
			logger.info("Starting tcsVoucherPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSendVoucher());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param14>{extra}</param14>";
				
				xml = xml.replace(replace, "");
				
			}
			
			if(jsonRequest.get("zone_id") == null || jsonRequest.get("zone_id").toString().isEmpty()) {
				
				replace = "";
				
				logger.info(String.format("No zone_id data is provided"));
				
				replace = "<param12>{zone_id}</param12>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapVoucherPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsB2AllCheckPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * 
		 */
		

		try {
			
			logger.info("Starting tcsB2AllCheckPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapB2AllCheckPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsB2AllPayment(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * 
		 */
		

		try {
			
			logger.info("Starting tcsB2AllPayment service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionPayment());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("account").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param1>%s</param1>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("account")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("account").toString());
				
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("extra") == null || jsonRequest.get("extra").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param7>{extra}</param7>";
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapB2AllPayment(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsMerchantRefund(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * 
		 */
		

		try {
			
			logger.info("Starting tcsMerchantRefund service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionRefund());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(jsonRequest.get("amount") == null) {
				
				logger.info(String.format("This is a full refund and amount is null"));
				
				replace = "<param21>{amount}</param21>";
				
				xml = xml.replace(replace, "");
				
				//logger.info(String.format("XML = %s", xml));
				
			}
			else {
				
				if(jsonRequest.get("amount").toString().isEmpty()) {
					
					logger.info(String.format("This is a full refund and amount is empty"));
					
					replace = "<param21></param21>";
					
					xml = xml.replace(replace, "");
					
					//logger.info(String.format("XML = %s", xml));
				}
			}
			
			logger.info("Calling TCS http client ..");
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapRefund(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsCheckSalesRequest(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * SALESREQUEST CheckOnly=true
		 */
		

		try {
			
			logger.info("Starting tcsCheckSalesRequest service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSalesRequest());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("destination").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param6>%s</param6>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "<param6></param6>");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("destination").toString());
				
				//A7A
				//xml = xml.replace(replace, "<param16></param16>");
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("remarks") == null || jsonRequest.get("remarks").toString().isEmpty()) {
				
				replace = "<param8>{remarks}</param8>";
				
				xml = xml.replace(replace, " ");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapCheckSalesRequest(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsSalesRequest(Map<String,Object> jsonRequest,Map<String, String> headers,boolean bulk_flag) {

		/*
		 * SALESREQUEST CheckOnly=false
		 */
		

		try {
			
			logger.info("Starting tcsSalesRequest service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSalesRequest());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("destination").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param6>%s</param6>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "<param6></param6>");
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("destination").toString());
				
				//A7A
				//xml = xml.replace(replace, "<param16></param16>");
				xml = xml.replace(replace, "");
			}
			
			if(jsonRequest.get("remarks") == null || jsonRequest.get("remarks").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param8>{remarks}</param8>";
				
				xml = xml.replace(replace, " ");
				
			}
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				Map<String,String> formattedResult = MappingParamsHelper.mapSalesRequest(result);
				
				formattedResult.put("Source_Account",String.format("%s", jsonRequest.get("source")));
				formattedResult.put("Destination_Account",String.format("%s", jsonRequest.get("destination")));
				formattedResult.put("Service_Code",String.format("%s", jsonRequest.get("code")));
				formattedResult.put("Brand_Id",String.format("%s", jsonRequest.get("brand")) );
				formattedResult.put("Requester_Remarks",String.format("%s", jsonRequest.get("remarks")) );
				formattedResult.put("Requested_By",String.format("%s", jsonRequest.get("username")) );
				formattedResult.put("Source_Id",String.format("%s", jsonRequest.get("source_id")) );
				formattedResult.put("Currency",String.format("%s", jsonRequest.get("currency")) );
				
				//Added 10042022 to support Bulk Requests
				//New Change 18042022 to add Bulk_Ref param
				if(bulk_flag) {
					
					formattedResult.put("Bulk_Flag","Y");
					formattedResult.put("Bulk_Ref",String.format("%s", jsonRequest.get("bulk_ref")) );
					makerCheckerService.createSQLBulkSalesRequest(formattedResult);
				}		
				else {
					
					formattedResult.put("Bulk_Flag","N");
					makerCheckerService.createSQLSalesRequest(formattedResult);
				}
					
				//////////////////////////////////////////
				
				
				
				return formattedResult;
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}	
	
	public Map<String,String> tcsSalesRequestCheck(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * SALESREQUESTCHECK API
		 */
		

		try {
			
			logger.info("Starting tcsSalesRequestCheck service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSalesRequestCheck());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapSalesRequestCheck(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsCheckSalesRequestExec(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * Deprecated as no Check mode is supported for SR Exec
		 */
		

		try {
			
			logger.info("Starting tcsCheckSalesRequestExec service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSalesRequestExec());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapCheckSalesRequestExec(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsSalesRequestExec(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * 
		 */
		

		try {
			
			logger.info("Starting tcsSalesRequestExec service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSalesRequestExec());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				
				Map<String,String> formattedResult = MappingParamsHelper.mapSalesRequestExec(result);
				
				formattedResult.put("Request_ID",jsonRequest.get("request_id").toString());
				formattedResult.put("Updated_By",jsonRequest.get("username").toString());					
				formattedResult.put("Remarks",jsonRequest.get("remarks").toString());
				
				if(formattedResult.get("Transaction_ID") == null) {
					
					//makerCheckerService.rejectSalesRequest(formattedResult);
				}
				else {
					
					//makerCheckerService.approveSalesRequest(formattedResult);
				}
				
				
				
				return formattedResult;
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsDummySalesRequestExec(Map<String,String> salesRequest) {

		/*
		 * 18072022
		 * 
		 * Added for multi checker flow to return sales request details instead of empty details 
		 */
		
		return MappingParamsHelper.mapDummySalesRequestExec(salesRequest);
		
		
	}
	
	public Map<String,String> tcsDummySalesRequestExec() {

		/*
		 * 06062022
		 * 
		 * Added for multi checker flow
		 */
		
		return MappingParamsHelper.mapDummySalesRequestExec();
		
		
	}
	
	public Map<String,String> tcsBusinessCashoutRequest2(Map<String,Object> jsonRequest,Map<String, String> headers,String approver_remarks) {

		/*
		 * 22082022
		 * 
		 * Added for the latest agreed workflow of creating CashOut SRs after Checker2 approval
		 * SALESREQUEST CheckOnly=false For Business Cash Out 
		 */
		

		try {
			
			logger.info("Starting tcsBusinessCashoutRequest2 service ..");
			
			
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTCSFunctionBCashOutSalesRequest());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("destination").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param6>%s</param6>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "<param6></param6>");
				
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("destination").toString());
				
				//A7A
				//xml = xml.replace(replace, "<param16></param16>");
				xml = xml.replace(replace, "");
			}
				
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				Map<String,String> formattedResult = MappingParamsHelper.mapSalesRequest(result);
				
				formattedResult.put("Source_Account",jsonRequest.get("source").toString());
				formattedResult.put("Destination_Account",jsonRequest.get("destination").toString());
				formattedResult.put("Service_Code","BZCASHOUT");
				formattedResult.put("Brand_Id",jsonRequest.get("brand").toString());
				formattedResult.put("Requester_Remarks",approver_remarks);
				formattedResult.put("Requested_By",jsonRequest.get("username").toString());
				formattedResult.put("Source_Id",jsonRequest.get("source_id").toString());
				formattedResult.put("Authorized_Person",jsonRequest.get("auth_person").toString());
				formattedResult.put("Authorized_ID",jsonRequest.get("auth_id").toString());
				formattedResult.put("Authorized_Mobile",jsonRequest.get("auth_mobile").toString());
				formattedResult.put("Currency",jsonRequest.get("currency").toString());
				formattedResult.put("Bulk_Flag","N");
				
				logger.info(String.format("Trying to update the internal database for local request id=%s ", jsonRequest.get("local_request_id")));
				
				Integer ar=makerCheckerService.approveInternalCashOutRequestChecker2(jsonRequest.get("local_request_id").toString(), 
						jsonRequest.get("user_id").toString(), 
						approver_remarks,
						formattedResult.get("Request_ID"));
				
				logger.info(String.format("Rows updated=%d ", ar));
				
				return formattedResult;
			
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsBusinessCashoutRequest(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * SALESREQUEST CheckOnly=false For Business Cash Out Only
		 */
		

		try {
			
			logger.info("Starting tcsBusinessCashoutRequest service ..");
			
			String delegate_data = String.format("%s#%s#%s", 
					jsonRequest.get("authorized_person").toString(),
					jsonRequest.get("authorized_id").toString(),
					jsonRequest.get("authorized_mobile").toString());
			
			String remarks = (String) jsonRequest.get("remarks");
			
			jsonRequest.put("remarks",delegate_data);
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSalesRequest());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("destination").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param6>%s</param6>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "<param6></param6>");
				
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("destination").toString());
				
				//A7A
				//xml = xml.replace(replace, "<param16></param16>");
				xml = xml.replace(replace, "");
			}
				
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				Map<String,String> formattedResult = MappingParamsHelper.mapSalesRequest(result);
				
				formattedResult.put("Source_Account",jsonRequest.get("source").toString());
				formattedResult.put("Destination_Account",jsonRequest.get("destination").toString());
				formattedResult.put("Service_Code",jsonRequest.get("code").toString());
				formattedResult.put("Brand_Id",jsonRequest.get("brand").toString());
				formattedResult.put("Requester_Remarks",remarks);
				formattedResult.put("Requested_By",jsonRequest.get("username").toString());
				formattedResult.put("Source_Id",jsonRequest.get("source_id").toString());
				formattedResult.put("Authorized_Person",jsonRequest.get("authorized_person").toString());
				if(jsonRequest.get("authorized_id") != null)
					formattedResult.put("Authorized_ID",jsonRequest.get("authorized_id").toString());
				if(jsonRequest.get("authorized_mobile") != null)
					formattedResult.put("Authorized_Mobile",jsonRequest.get("authorized_mobile").toString());
				formattedResult.put("Currency",jsonRequest.get("currency").toString());
				formattedResult.put("Bulk_Flag","N");
				
				makerCheckerService.createSQLBusinessCashoutRequest(formattedResult);
				
				return formattedResult;
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsBusinessCashoutRequest_BAK(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * This is a copy of tcsBusinessCashoutRequest function for backup purposes 01062022
		 * 
		 * SALESREQUEST CheckOnly=false For Business Cash Out Only
		 */
		

		try {
			
			logger.info("Starting tcsBusinessCashoutRequest service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSalesRequest());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("destination").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param6>%s</param6>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "<param6></param6>");
				
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("destination").toString());
				
				//A7A
				//xml = xml.replace(replace, "<param16></param16>");
				xml = xml.replace(replace, "");
			}
			
			jsonRequest.put("remarks",jsonRequest.get("authorized_person").toString());
			
			
			if(jsonRequest.get("remarks") == null || jsonRequest.get("remarks").toString().isEmpty()) {
				
				logger.info(String.format("No extra data is provided"));
				
				replace = "<param8>{remarks}</param8>";
				
				xml = xml.replace(replace, " ");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				Map<String,String> formattedResult = MappingParamsHelper.mapSalesRequest(result);
				
				formattedResult.put("Source_Account",jsonRequest.get("source").toString());
				formattedResult.put("Destination_Account",jsonRequest.get("destination").toString());
				formattedResult.put("Service_Code",jsonRequest.get("code").toString());
				formattedResult.put("Brand_Id",jsonRequest.get("brand").toString());
				formattedResult.put("Requester_Remarks",jsonRequest.get("remarks").toString());
				formattedResult.put("Requested_By",jsonRequest.get("username").toString());
				formattedResult.put("Source_Id",jsonRequest.get("source_id").toString());
				formattedResult.put("Authorized_Person",jsonRequest.get("authorized_person").toString());
				if(jsonRequest.get("authorized_id") != null)
					formattedResult.put("Authorized_ID",jsonRequest.get("authorized_id").toString());
				if(jsonRequest.get("authorized_mobile") != null)
					formattedResult.put("Authorized_Mobile",jsonRequest.get("authorized_mobile").toString());
				formattedResult.put("Currency",jsonRequest.get("currency").toString());
				formattedResult.put("Bulk_Flag","N");
				
				makerCheckerService.createSQLBusinessCashoutRequest(formattedResult);
				
				return formattedResult;
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsCheckBusinessCashoutRequest(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * SALESREQUEST CheckOnly=True API For Business Cash Out Only
		 */
		

		try {
			
			logger.info("Starting tcsCheckBusinessCashoutRequest service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSalesRequest());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(isTillCode(jsonRequest.get("destination").toString())) {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				logger.info(String.format("Account provided is Till Code"));
				
				replace = String.format("<param6>%s</param6>", jsonRequest.get("destination").toString());
				
				xml = xml.replace(replace, "<param6></param6>");
				
				
			}
			else {
				
				logger.info(String.format("Account provided is %s", jsonRequest.get("destination")));
				
				replace = String.format("<param16>%s</param16>", jsonRequest.get("destination").toString());
				
				//A7A
				//xml = xml.replace(replace, "<param16></param16>");
				xml = xml.replace(replace, "");
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				return MappingParamsHelper.mapCheckSalesRequest(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsBusinessCashoutRequestExec(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * This function is only used for Business Cash Out Scenario
		 * where the Approval is done by the Destination Agent
		 */
		

		try {
			
			logger.info("Starting tcsBusinessCashoutRequestExec service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSalesRequestExec());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				
				Map<String,String> formattedResult = MappingParamsHelper.mapSalesRequestExec(result);
				
				formattedResult.put("Request_ID",jsonRequest.get("request_id").toString());
				formattedResult.put("Updated_By",jsonRequest.get("username").toString());					
				formattedResult.put("Remarks",jsonRequest.get("remarks").toString());
				
				//Check if Transaction_ID exists which means the Sales Rqst is actually Approved on TCS
				if(formattedResult.get("Transaction_ID") == null) {
					
					//makerCheckerService.rejectSalesRequest(formattedResult);
				}
				else {
					
					//makerCheckerService.approveSalesRequest(formattedResult);
				}
				
				
				
				return formattedResult;
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	
	private Boolean isTillCode(String target) {
		
		return (target.length()<=6);
	}
	
	public Map<String,Object> checkPaymentGatewayBalance(Map<String,Object> jsonRequest,Map<String, String> headers) {

		/*
		 * To forward inquiries to Payment Gateway
		 * 
		 * Added 16-04-2022
		 */
		
		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getPgFunctionInquiry());
		
		String uri = String.format("%s", tcsServiceInitializer.getPgHost());

		try {
			
			logger.info("Starting checkPaymentGatewayBalance service ..");
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("Payment Gateway Result: %s",result));
			
			System.out.println(result.get("Message"));
			
			//Return Result
			Map<String,Object> r = new HashMap<>();
			
			r.put("Result","0");
			//r.put("Message",String.format("%s", result.get("Message")).replace("\\\\",""));
			r.put("Pg_Result",result);
			
			return r;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,Object> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,Object> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Integer addNewRates(Map<String,Object> entity) {
		
		return forexSqlDao.insertNewForex(entity);
	}
	
	public Map<String,Object> getLatestForexRates(){
		
		List<Map<String,Object>> result = forexSqlDao.getLatestForexRate();
		
		if(result != null && result.size()==1) {
			
			logger.info(String.format("Rates found : %s", result));
			return result.get(0);
		}
		
		return null;
	}
}
