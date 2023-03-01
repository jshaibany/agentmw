package com.onecashye.web.security.service;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import com.onecashye.web.security.helper.YkbJsonHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;

@Service
@PropertySource({ "classpath:application.properties" })
public class YkbHttpClientService {

	@Autowired
    private Environment env;
	
	@Value("${rsa.private.key}")
	private String rsaPrivateKeyFile;
	
	private HttpClient getHttpClient() {
		
		return HttpClientService.getHttpClient(Integer.decode(env.getProperty("ykb.timeout")));
		
	}

	public Map<String,Object> balanceQuery(String ac,String sc,String sno){
		
		
		WebClient client = WebClient.builder()
				  .baseUrl("")
				  .defaultCookie("cookieKey", "cookieValue")
				  .defaultUriVariables(Collections.singletonMap("url", ""))
				  .clientConnector(new ReactorClientHttpConnector(getHttpClient()))
				  .build();
		
		
		client.method(HttpMethod.GET);
		
		try {
			
			
			
			Mono<String> sr = client.get().uri(uriBuilder -> uriBuilder
					.scheme(env.getProperty("ykb.scheme"))
					.host(env.getProperty("ykb.host"))
					.port(env.getProperty("ykb.port"))
					.path(env.getProperty("ykb.path"))
					.queryParam("ac",ac)
					.queryParam("sc",sc)
					.queryParam("sno",sno)
					.queryParam("usr",env.getProperty("ykb.usr"))
					.queryParam("tkn",RSASecurityService.decrypt(env.getProperty("ykb.tkn"), rsaPrivateKeyFile))
					.build())
					.retrieve()
					.bodyToMono(String.class);
			
			
			//System.out.println(sr.block());
			
			return YkbJsonHelper.mapJsonString(sr.block());
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			Map<String,Object> er = new HashMap<>();
			er.put("error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			e.printStackTrace();
			Map<String,Object> er = new HashMap<>();
			er.put("error", e.getMessage());
			return er;
		}
		
	}
}
