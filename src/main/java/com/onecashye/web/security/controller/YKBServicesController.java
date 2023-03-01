package com.onecashye.web.security.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.onecashye.web.security.helper.YkbLookupMapper;
import com.onecashye.web.security.middleware.dao.BillerDao;
import com.onecashye.web.security.service.YkbHttpClientService;

@RestController
public class YKBServicesController {

	Logger logger = LogManager.getLogger(YKBServicesController.class);
	
	private final YkbHttpClientService ykbHttpClientService;
	private final BillerDao billerDao;
	
	@Autowired
	public YKBServicesController(YkbHttpClientService ykbHttpClientService, BillerDao billerDao) {
		super();
		this.ykbHttpClientService = ykbHttpClientService;
		this.billerDao = billerDao;
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/mobile/topup/enquiry")
	@ResponseBody
	public Map<String,Object> mobileTopupEnquiry(@RequestParam String number,
			@RequestParam String type,
			@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		switch("YKB") {
		
		case "YKB":
			
			try {
				
				String serviceCode = YkbLookupMapper.getMobileServiceCode(number);
				
				Map<String,Object> ykb = ykbHttpClientService
						.balanceQuery("4001", serviceCode, number);
				
				try {
					
					response.put("Provider", billerDao.getBillerDefinition("YKB", serviceCode, type));
					
					@SuppressWarnings("unchecked")
					List<Map<String,Object>> provider = (List<Map<String,Object>>) response.get("Provider");
					
					response.put("Topup", billerDao.getBillerDetails((Integer)provider.get(0).get("Biller_Id"),"TOPUP"));
				}
				catch(Exception e) {
					
					e.printStackTrace();
					
					response.put("Result", String.format("%d", -100));
					response.put("Message", e.getMessage());
					
					return response;
				}
				
				response.put("Result", 0);
				response.put("Message", "");
				response.put("Enquiry", ykb);
				
				return response;
			}
			catch(Exception e) {
				
				e.printStackTrace();
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", e.getMessage());
				
				return response;
			}
		case "YP":

			try {
				
				return null;
			}
			catch(Exception e) {
				
				e.printStackTrace();
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", e.getMessage());
				
				return response;
			}
			
		default:
			return null;
		}
		
		
		
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/mobile/bundle/enquiry")
	@ResponseBody
	public Map<String,Object> mobileBundleEnquiry(@RequestParam String number,
			@RequestParam String type,
			@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		switch("YKB") {
		
		case "YKB":
			
			try {
				
				String serviceCode = YkbLookupMapper.getMobileServiceCode(number);
				
				Map<String,Object> ykb = ykbHttpClientService
						.balanceQuery("4001", serviceCode, number);
				
				try {
					
					response.put("Provider", billerDao.getBillerDefinition("YKB", serviceCode, type));
					
					@SuppressWarnings("unchecked")
					List<Map<String,Object>> provider = (List<Map<String,Object>>) response.get("Provider");
					
					response.put("Bundles", billerDao.getBillerDetails((Integer)provider.get(0).get("Biller_Id"),"BUNDLE"));
				}
				catch(Exception e) {
					
					e.printStackTrace();
					
					response.put("Result", String.format("%d", -100));
					response.put("Message", e.getMessage());
					
					return response;
				}
				
				response.put("Result", 0);
				response.put("Message", "");
				response.put("Enquiry", ykb);
				
				return response;
			}
			catch(Exception e) {
				
				e.printStackTrace();
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", e.getMessage());
				
				return response;
			}
		case "YP":

			try {
				
				return null;
			}
			catch(Exception e) {
				
				e.printStackTrace();
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", e.getMessage());
				
				return response;
			}
			
		default:
			return null;
		}
		
		
		
		
	}
}
