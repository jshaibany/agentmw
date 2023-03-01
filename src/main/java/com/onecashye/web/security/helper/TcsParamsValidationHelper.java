package com.onecashye.web.security.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

import com.onecashye.web.security.exception.NullOrEmptyInputParameters;

public class TcsParamsValidationHelper {

	public static void checkRequiredParams(Environment env,String property,Map<String,Object> jsonRequest,Logger logger) throws NullOrEmptyInputParameters {
		
		/*
		 * 31052022
		 * 
		 * This function takes a comma separated string of mandatory params @property
		 * and check each param with the input @jsonRequest
		 * 
		 * if null or empty is found a NullOrEmptyInputParameters exception is thrown
		 */
		
		String config_params = env.getProperty(property);
		List<String> required_params = Arrays.asList(config_params.split(",", -1));
		List<String> fail_params = new ArrayList<>();
		
		for(String p : required_params) {
			
			if(jsonRequest.get(p.trim()) == null)
				fail_params.add(p.trim());
				
		}
		
		if(fail_params.size()>0) {
			
			logger.warn("Some input params are null ..");
			
			for(String p : fail_params) {
				
				logger.error(String.format("Parameter %s is null ...", p));
			}
			throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
		}
		
		for(String p : required_params) {
			
			if(jsonRequest.get(p.trim()).toString().isEmpty())
				fail_params.add(p.trim());
		}
		
		if(fail_params.size()>0) {
			
			logger.warn("Some input params are empty ..");
			
			for(String p : fail_params) {
				
				logger.error(String.format("Parameter %s is empty", p));
			}
			throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
		}
			
	}

}
