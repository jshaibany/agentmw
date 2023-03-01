package com.onecashye.web.security.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClient.UriSpec;

import com.onecashye.web.security.helper.JsonHelper;
import com.onecashye.web.security.helper.XmlToMapConverter;


import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;


public class TcsHttpClientService {

	Logger logger = LogManager.getLogger(TcsHttpClientService.class);
	
	public static Map<String,String> tcsCall(String uri,String xml,HttpClient httpClient) throws Exception{
		
		WebClient client = WebClient.builder()
				  .baseUrl("")
				  .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML_VALUE) 
				  .defaultUriVariables(Collections.singletonMap("url", ""))
				  .clientConnector(new ReactorClientHttpConnector(httpClient))
				  .build();
		
		UriSpec<RequestBodySpec> uriSpec = client.method(HttpMethod.POST);
		
		RequestBodySpec bodySpec = uriSpec.uri(URI.create(uri));
		
		RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue(xml);
		
		ResponseSpec responseSpec = headersSpec.header(
			    HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML_VALUE)
			  .accept(MediaType.APPLICATION_XML)
			  .acceptCharset(StandardCharsets.UTF_8)
			  .ifNoneMatch("*")
			  .ifModifiedSince(ZonedDateTime.now())
			  .retrieve();
		
		Mono<String> rs2 = responseSpec.bodyToMono(String.class);
		
		Map<String,String> result = XmlToMapConverter.convertXmlDocumentToMap(rs2.block());
		
		return result;
	}

	public static Map<String,Object> jsonRpcDoLogin(String user,String pass,String domain,String Uri,String path) throws Exception {
		
		
		String json = "{" + 
				"    \"method\": \"login.doLogin\"," + 
				"    \"params\": [" + 
				"        \""+user+"\"," + 
				"        \""+pass+"\"," +  
				"        \"EN\"," + 
				"        \"XAF4ss7hmS0D8Qe5umrttnb9KxKKrUzGiDcPvOkPifZJ9MrPE4htPFE5JJ6M4vhC\"" + 
				"    ]," + 
				"    \"id\": 1" + 
				"}";
	    StringEntity entity = new StringEntity(json);
	    
	    BasicCookieStore cookieStore = new BasicCookieStore();
	    BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", "");
	    
	    cookie.setDomain(domain);
	    cookie.setPath(path);
	    cookie.setSecure(false);
	    cookieStore.addCookie(cookie);
	   
	    CloseableHttpClient httpClient = HttpClients
	    	    .custom()
	    	    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
	    	    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
	    	    .setDefaultCookieStore(cookieStore)
	    	    .build();
	    
		HttpUriRequest request = RequestBuilder.get()
				  .setUri(Uri)
				  .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				  .setHeader(HttpHeaders.ACCEPT, "*/*")
				  .setHeader(HttpHeaders.ACCEPT_CHARSET, "charset=UTF-8")
				  .setEntity(entity)
				  .build();
		
		
	    
	    CloseableHttpResponse response = httpClient.execute(request);
	    
	    String bodyAsString = EntityUtils.toString(response.getEntity());
	    
	    System.out.println("RPC DoLogin: "+bodyAsString);
	    
	    Map<String,Object> result = JsonHelper.mapJsonString(bodyAsString);
	    
	    if(result.get("result").toString().equalsIgnoreCase("0")) {
			
	    	//Success login
			
	    	Header[] headers = response.getHeaders(HttpHeaders.SET_COOKIE);
		    HeaderElement[] elements = headers[0].getElements();
		       
		    result.put("Session", elements[0].getValue());
		    
		    httpClient.close();
		    
		    return result;
		}
	    
	    return null;
	    
	    
	}
	
	public static Map<String,Object> jsonRpcDoLogout(String c,String domain,String Uri,String path) throws Exception {
		
		/*
		 * Added 12042022 to Logout User, Fix bug: Maximum Sessions Reached
		 */
		
		String json = "{" + 
				"    \"method\": \"login.doLogout\"," + 
				"    \"params\": []," + 
				"    \"id\": 1" + 
				"}";
		
	    StringEntity entity = new StringEntity(json);
	    
	    BasicCookieStore cookieStore = new BasicCookieStore();
	    BasicClientCookie cookie = new BasicClientCookie("JSESSIONID",c);
	    
	    cookie.setDomain(domain);
	    cookie.setPath(path);
	    cookie.setSecure(false);
	    cookieStore.addCookie(cookie);
	   
	    CloseableHttpClient httpClient = HttpClients
	    	    .custom()
	    	    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
	    	    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
	    	    .setDefaultCookieStore(cookieStore)
	    	    .build();
		
		HttpUriRequest request = RequestBuilder.get()
				  .setUri(Uri)
				  .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				  .setHeader(HttpHeaders.ACCEPT, "*/*")
				  .setHeader(HttpHeaders.ACCEPT_CHARSET, "charset=UTF-8")
				  .setEntity(entity)
				  .build();
		
		
	    
	    CloseableHttpResponse response = httpClient.execute(request);
	    
	    String bodyAsString = EntityUtils.toString(response.getEntity());
	    
	    httpClient.close();
	    
	    return JsonHelper.mapJsonString(bodyAsString);
	    
	    
	}

	public static Map<String,Object> jsonRpcCall(String json, String c,String domain,String Uri,String path) throws Exception {
		
		
	    StringEntity entity = new StringEntity(json);
	    
	    BasicCookieStore cookieStore = new BasicCookieStore();
	    BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", c);

	    cookie.setDomain(domain);
	    cookie.setPath(path);
	    cookie.setSecure(false);
	    cookieStore.addCookie(cookie);
	    	   
	    CloseableHttpClient httpClient = HttpClients
	    	    .custom()
	    	    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
	    	    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
	    	    .setDefaultCookieStore(cookieStore)
	    	    .build();
		
		HttpUriRequest request = RequestBuilder.get()
				  .setUri(Uri)
				  .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				  .setHeader(HttpHeaders.ACCEPT, "*/*")
				  .setHeader(HttpHeaders.ACCEPT_CHARSET, "charset=UTF-8")
				  .setEntity(entity)
				  .build();
		
		
	    
	    CloseableHttpResponse response = httpClient.execute(request);
	    
	    String bodyAsString = EntityUtils.toString(response.getEntity());
	    
	    httpClient.close();
	    
	    return JsonHelper.mapJsonString(bodyAsString);
	}
	
}
