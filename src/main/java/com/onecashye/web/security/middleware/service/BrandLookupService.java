package com.onecashye.web.security.middleware.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.onecashye.web.security.middleware.dao.BrandLookupDao;

@Service
public class BrandLookupService {

	private final BrandLookupDao brandLookupDao;
	
	@Autowired
	public BrandLookupService(BrandLookupDao brandLookupDao) {
		
		super();
		this.brandLookupDao = brandLookupDao;
		
	}
	
	public Optional<List<Map<String,Object>>> findBrand(String trx_type,String currency){
		
		//List<Map<String,Object>> r = brandLookupDao.getBrand(trx_type, currency);
		
		List<Map<String,Object>> r = brandLookupDao.getBrand(trx_type+"."+currency);
		
		if(r.size() <= 0)
			return Optional.ofNullable(null);
		else
			return Optional.ofNullable(r);
		
		
	}
}
