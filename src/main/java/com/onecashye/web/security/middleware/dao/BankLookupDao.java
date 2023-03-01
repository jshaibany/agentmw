package com.onecashye.web.security.middleware.dao;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
@PropertySources({
    @PropertySource("classpath:dao.properties"),
    @PropertySource("classpath:tcs.properties")
})
public class BankLookupDao {

	private final EntityManager entityManager;
	@SuppressWarnings("unused")
	private final Environment env;
	
	@Autowired
	public BankLookupDao(@Qualifier("middlewareEM") EntityManager entityManager, Environment env) {
		
		this.entityManager=entityManager;
		this.env = env;
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> getBankDefinitions(String currency){
		
		/*
		 * get Bank definitions
		 */
		
		entityManager.getProperties();
		
		try {
			
			//URL res = getClass().getClassLoader().getResource(env.getProperty("BankYER"));
			//File file= Paths.get(res.toURI()).toFile();
			
			String resName="Bank"+currency;
			
			File resource = new ClassPathResource(resName).getFile();
			System.out.println(resource.getAbsolutePath());
			
			byte[] bankFileBytes= Files.readAllBytes(resource.toPath());
			String s = new String(bankFileBytes, StandardCharsets.UTF_8);
			System.out.println(s);
			Map<String, Object> result;
			
			result = new ObjectMapper().readValue(s, HashMap.class);
			
			return (List<Map<String, Object>>) result.get("Banks");
			
			
			
		} catch (IOException e1) {
			
			e1.printStackTrace();
			return null;
		}
        
		/*
		try {
			File bankFile = new File(env.getProperty("BankYER"));
			byte[] bankFileBytes;
			bankFileBytes = Files.readAllBytes(bankFile.toPath());
			String s = new String(bankFileBytes, StandardCharsets.UTF_8);
			System.out.println(s);
			Map<String, Object> result;
			
			try {
				
				result = new ObjectMapper().readValue(s, HashMap.class);
				
				return (List<Map<String, Object>>) result.get("Banks");
				
			} catch (JsonMappingException e) {
				
				e.printStackTrace();
			} catch (JsonProcessingException e) {
			
				e.printStackTrace();
			}
			return null;
			
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		
		
		String path="";
		
		if(currency.contentEquals("YER"))
			path=env.getProperty("BankYER");
		if(currency.contentEquals("USD"))
			path=env.getProperty("BankUSD");
		
		// Declaring object of StringBuilder class
        StringBuilder builder = new StringBuilder();
 
        // try block to check for exceptions where
        // object of BufferedReader class us created
        // to read filepath
        try (BufferedReader buffer = new BufferedReader(
                 new FileReader(path))) {
 
            String str;
 
            // Condition check via buffer.readLine() method
            // holding true upto that the while loop runs
            while ((str = buffer.readLine()) != null) {
 
                builder.append(str).append("\n");
            }
        }
 
        // Catch block to handle the exceptions
        catch (IOException e) {
 
            // Print the line number here exception occurred
            // using printStackTrace() method
            e.printStackTrace();
        }
 
        // Returning a string
        String f = builder.toString();
		
		Map<String, Object> result;
		
		try {
			
			result = new ObjectMapper().readValue(f, HashMap.class);
			
			return (List<Map<String, Object>>) result.get("Banks");
			
		} catch (JsonMappingException e) {
			
			e.printStackTrace();
		} catch (JsonProcessingException e) {
		
			e.printStackTrace();
		}
		return null;
		
		/*
		List<Object[]> requests = entityManager.createNativeQuery(
				 "SELECT * "+ 
				 "FROM "+env.getProperty("table.lookup.banks")+" AS WBD " + 
				 "WHERE WBD.STATUS = 'A' "+
				 "AND WBD.CURRENCY = :currency ")
				.setParameter("currency", currency)
				.getResultList();
	
		
		List<Map<String,Object>> result = new ArrayList<>();
		
		for(Object[] request : requests) {
			
			Map<String,Object> r = new HashMap<>();
			
			r.put("Bank_Code",(String) request[0]);
			r.put("Name_AR",(String) request[1]);
			r.put("Name_EN",(String) request[2]);
			r.put("Brand",(Integer) request[3]);
			
			
		    result.add(r);
		    
		}
		
	
		return result;
		*/
	
	}

	@SuppressWarnings({ "unchecked" })
	public List<Map<String,Object>> getForexBankDefinitions(){
		
		/*
		 * get Forex Bank definitions
		 */
		
		entityManager.getProperties();
		
		try {
			
			String resName="ForexBankYER";
			
			File resource = new ClassPathResource(resName).getFile();
			System.out.println(resource.getAbsolutePath());
			
			byte[] bankFileBytes= Files.readAllBytes(resource.toPath());
			String s = new String(bankFileBytes, StandardCharsets.UTF_8);
			System.out.println(s);
			Map<String, Object> result;
			
			result = new ObjectMapper().readValue(s, HashMap.class);
			
			return (List<Map<String, Object>>) result.get("Banks");
			
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}
        
		
	
	}
}
