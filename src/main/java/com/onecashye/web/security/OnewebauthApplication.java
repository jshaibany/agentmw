package com.onecashye.web.security;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class OnewebauthApplication  {

	public static void main(String[] args) {
	    
		SpringApplication.run(OnewebauthApplication.class, args);
	}

	
}
