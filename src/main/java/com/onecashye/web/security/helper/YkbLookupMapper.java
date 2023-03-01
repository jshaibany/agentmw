package com.onecashye.web.security.helper;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YkbLookupMapper {


	public static List<Map<String,Object>> getServiceCodes() throws JsonMappingException, JsonProcessingException {
		
		
		String json="[" + 
				" {" + 
				"   \"Service_Code\": 42101," + 
				"   \"Desc_En\": \"Sabafon\"," + 
				"   \"Desc_Ar\": \"سبأفون\"" + 
				" }," + 
				" {" + 
				"   \"Service_Code\": 42102," + 
				"   \"Desc_En\": \"MTN\"," + 
				"   \"Desc_Ar\": \"إم تي إن\"" + 
				" }," + 
				" {" + 
				"   \"Service_Code\": 42103," + 
				"   \"Desc_En\": \"Yemen Mobile\"," + 
				"   \"Desc_Ar\": \"يمن موبايل\"" + 
				" }," + 
				" {" + 
				"   \"Service_Code\": 42104," + 
				"   \"Desc_En\": \"Y\"," + 
				"   \"Desc_Ar\": \"واي\"" + 
				" }," + 
				" {" + 
				"   \"Service_Code\": 42105," + 
				"   \"Desc_En\": \"ADSL\"," + 
				"   \"Desc_Ar\": \"الانترنت المنزلي\"" + 
				" }," + 
				" {" + 
				"   \"Service_Code\": 42106," + 
				"   \"Desc_En\": \"Land Phone\"," + 
				"   \"Desc_Ar\": \"التلفون الأرضي\"" + 
				" }," + 
				" {" + 
				"   \"Service_Code\": 42107," + 
				"   \"Desc_En\": \"Water Utility\"," + 
				"   \"Desc_Ar\": \"الماء\"" + 
				" }," + 
				" {" + 
				"   \"Service_Code\": 42108," + 
				"   \"Desc_En\": \"Electricity\"," + 
				"   \"Desc_Ar\": \"الكهرباء\"" + 
				" }," + 
				" {" + 
				"   \"Service_Code\": 42109," + 
				"   \"Desc_En\": \"Teleyemen\"," + 
				"   \"Desc_Ar\": \"تيليمن\"" + 
				" }," + 
				" {" + 
				"   \"Service_Code\": 42112," + 
				"   \"Desc_En\": \"Aden Net\"," + 
				"   \"Desc_Ar\": \"عدن نت\"" + 
				" }" + 
				"]";
		
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> data = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
		
		return data;
	}

	public static String getMobileServiceCode(String number) {
		
		if(number.length()==9) {
			
			String init = number.substring(0, 2);
			
			switch(init) {
			
			case "77":
				return "42103";
				
			case "71":
				return "42101";
				
			case "73":
				return "42102";
				
			case "70":
				return "42104";
				
			}
		}
		
		return null;
	}
}
