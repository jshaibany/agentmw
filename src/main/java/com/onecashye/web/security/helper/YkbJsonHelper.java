package com.onecashye.web.security.helper;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YkbJsonHelper {

	@SuppressWarnings("unchecked")
	public static Map<String,Object> mapJsonString(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		Map<String, Object> map;
		try {
			map = mapper.readValue(json, Map.class);
			return map;
		} catch (JsonMappingException e) {
			
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
}
