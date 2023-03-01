package com.onecashye.web.security.telepin.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.onecashye.web.security.telepin.dao.AccountDao;

@Service
public class AccountService {

	private final AccountDao accountDao;
	
	@Autowired
	public AccountService(AccountDao accountDao) {
		this.accountDao = accountDao;
		
	}
	
	public String getAccountUniversalID(String account_id) {
		
		try {
			
			return accountDao.getAccountUniversalID(account_id);
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return "";
		}
	}
	
	public List<Map<String,Object>> getAccountChildren(String parent_id) {
		
		try {
			
			return accountDao.getAccountChildren(parent_id);
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return null;
		}
	}
	
	public List<Map<String,Object>> getAccountChildrenForCashupReport(String parent_id) {
		
		try {
			
			return accountDao.getAccountChildrenForCashupReport(parent_id);
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return null;
		}
	}
	
	public List<Map<String,Object>> getAccountNominatedDetails(String account_id) {
		
		try {
			
			return accountDao.getAccountNominatedDetails(account_id);
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
			return null;
		}
	}
}
