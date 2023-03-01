package com.onecashye.web.security.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;

import com.onecashye.web.security.helper.MatcherHelper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Service
public class TcsApiOtpService {

	private final TcsServiceInitializer tcsServiceInitializer;
	private HttpClient httpClient;
	
	@Autowired
	public TcsApiOtpService(TcsServiceInitializer tcsServiceInitializer,
			JwtSecurityService securityService) {
		super();
		this.tcsServiceInitializer=tcsServiceInitializer;
		httpClient = HttpClient.create()
				  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, tcsServiceInitializer.getTcsCallTimeout())
				  .responseTimeout(Duration.ofMillis(tcsServiceInitializer.getTcsCallTimeout()))
				  .doOnConnected(conn -> 
				    conn.addHandlerLast(new ReadTimeoutHandler(30))
				      .addHandlerLast(new WriteTimeoutHandler(tcsServiceInitializer.getTcsCallTimeout(), TimeUnit.MILLISECONDS)));
	}
	
	public Map<String,String> tcsRequestTac(Map<String,Object> jsonRequest) {
		
		jsonRequest.put("username",tcsServiceInitializer.getTcsProxyUser());
		jsonRequest.put("password",tcsServiceInitializer.getTcsProxyPassword());

		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionRequestTac());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				Map<String,String> response = new HashMap<>();
				
				response.put("Result","0");
				response.put("Message","TAC_SENT");
				
				return response;
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message","FAIL_TAC");
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsValidateTac(Map<String,Object> jsonRequest) {
		
		jsonRequest.put("username",tcsServiceInitializer.getTcsProxyUser());
		jsonRequest.put("password",tcsServiceInitializer.getTcsProxyPassword());

		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionValidateTac());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0") && result.get("param1").equalsIgnoreCase("1")) {
				

				Map<String,String> response = new HashMap<>();
				
				response.put("Result","0");
				response.put("Message","VALID_TAC");
				
				return response;
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message","INVALID_TAC");
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}

}
