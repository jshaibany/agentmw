package com.onecashye.web.security.telepin.service;

import java.util.List;
import java.util.Map;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.onecashye.web.security.telepin.dao.TerminalDao;


@Service
public class TerminalService {

	
	private final TerminalDao terminalDao;
	
	@Autowired
	public TerminalService(TerminalDao terminalDao) {
		
		super();
		this.terminalDao = terminalDao;
	}
	
	
	public Map<String,Object> getTerminalDetails(String username,String account_id){
		
		List<Map<String,Object>> r = terminalDao.getTerminalDetails(username, account_id);
		
		if(r != null && r.size() == 1) {
			
			return r.get(0);
		}
		
		return null;
	}
	
	public Map<String,Object> getUserNotificationNumber(String username){
		
		List<Map<String,Object>> r = terminalDao.getUserNotificationNumber(username);
		
		if(r != null && r.size() == 1) {
			
			return r.get(0);
		}
		
		return null;
	}
	
	public List<Map<String,Object>> getAccountTerminals(String account_id,String terminal_type){
		
		List<Map<String,Object>> r = terminalDao.getAccountTerminals(account_id, terminal_type);
		
		return r;
		
		//return null;
	}
	
	public List<Map<String,Object>> getAccountTerminals(String username, String account_id,String terminal_type){
		
		//Overload getAccountTerminals to support filtering
		
		List<Map<String,Object>> r = terminalDao.getAccountTerminals(username, account_id, terminal_type);
		
		return r;
	}
	
	public List<Map<String,Object>> getAccountTerminals(List<String> privileges,String username, String account_id,String terminal_type){
		
		//Overload getAccountTerminals to support filtering
		
		List<Map<String,Object>> r = terminalDao.getAccountTerminals(privileges,username, account_id, terminal_type);
		
		return r;
	}
	
	public List<Map<String,Object>> getAccountWebTerminals4Cashup(List<String> privileges,String username, String account_id,String terminal_type){
		
		//Added 13052022
		//To provide droplist values for cashup report (Web Terminals)
		
		List<Map<String,Object>> r = terminalDao.getAccountWebTerminals4Cashup(privileges,username, account_id, terminal_type);
		
		return r;
	}
	
	public List<Map<String,Object>> getAccountAppTerminals4Cashup(List<String> privileges,String username, String account_id,String terminal_type){
		
		//Added 13052022
		//To provide droplist values for cashup report (Staff)
		
		List<Map<String,Object>> r = terminalDao.getAccountAppTerminals4Cashup(privileges,username, account_id, terminal_type);
		
		return r;
	}
	
	public List<Map<String,Object>> getAccountTerminals(String account_id){
		
		//Added 13042022 for Edgecom Cashup Report Filters
		
		List<Map<String,Object>> r = terminalDao.getAccountTerminals(account_id);
		
		return r;
	}
	
	public List<Map<String,Object>> getSelfTerminal(String account_id, String user_name){
		
		//Added 13042022 for Edgecom Cashup Report Filters
		
		List<Map<String,Object>> r = terminalDao.getSelfTerminal(account_id, user_name);
		
		return r;
	}
	
	public List<Map<String,Object>> getAccountGptStaff(String account_id){
		
		//Added 13042022 for Edgecom Cashup Report Filters
		
		List<Map<String,Object>> r = terminalDao.getAccountGptStaff(account_id);
		
		return r;
	}
	
	public List<Map<String,Object>> getTerminalTypesLookup(String username, String account_id,String terminal_type,String level){
		
		/*
		 * Create 16052022
		 * 
		 * To get role names for drop lists
		 */
		switch(terminal_type) {
		
		case "WEB":
			
			if(level.contentEquals("WebRoleMasterManager")) {
				
				List<Map<String,Object>> r = terminalDao.getWebTerminalTypesLookup4Master(username,account_id);
				
				return r;
			}
			else {
				
				List<Map<String,Object>> r = terminalDao.getWebTerminalTypesLookup4Manager(username,account_id);
				
				return r;
			}
				
				
			
		case "GPT":
		
			if(level.contentEquals("WebRoleMasterManager")) {
				
				List<Map<String,Object>> r = terminalDao.getGptTerminalTypesLookup4Master(username,account_id);
				
				return r;
			}
			else {
				
				List<Map<String,Object>> r = terminalDao.getGptTerminalTypesLookup4Manager(username,account_id);
				
				return r;
			}
			
		default:
			List<Map<String,Object>> r = terminalDao.getGptTerminalTypesLookup4Manager(username,account_id);
			
			return r;
		
		}
		
		
	}
	
	public Boolean isTerminalExisted(String terminal_user) throws Exception {
		
		String count = terminalDao.countActiveTerminal(terminal_user);
		
		if(count.equals("-1"))
			throw new Exception("EXCEPTION_IN_TERMINAL_QUERY");
		
		return !count.contentEquals("0");
	}
	
	public String getUserMsisdnByAlias(String alias) {
		
		//Added 02102022
		List<String> result = terminalDao.getUserMsisdnByAlias(alias);
		
		if(result != null && result.size()>0) {
			
			return result.get(0);
		
		}
		
		return "";
	}
}
