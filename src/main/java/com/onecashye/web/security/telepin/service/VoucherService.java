package com.onecashye.web.security.telepin.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.onecashye.web.security.telepin.dao.VoucherDao;

@Service
public class VoucherService {

	//Added this service 23-03-2022
	private final VoucherDao  voucherDao;
	
	@Autowired
	public VoucherService(VoucherDao voucherDao) {
		
		super();
		this.voucherDao = voucherDao;
	}
	
	public Optional<List<Map<String,Object>>> getVoucherDetails(String sales_order){
		
		List<Map<String,Object>> r = voucherDao.getVoucherDetails(sales_order);
		
		if(r.size()<=0)
			return Optional.ofNullable(null);
		
		return Optional.ofNullable(r);
	}
}
