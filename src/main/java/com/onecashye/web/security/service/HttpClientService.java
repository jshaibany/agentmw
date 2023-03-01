package com.onecashye.web.security.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

public class HttpClientService {
	
	public static HttpClient getHttpClient(Integer tcsCallTimeout) {
		
		return HttpClient.create()
				  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, tcsCallTimeout)
				  .responseTimeout(Duration.ofMillis(tcsCallTimeout))
				  .doOnConnected(conn -> 
				    conn.addHandlerLast(new ReadTimeoutHandler(30))
				      .addHandlerLast(new WriteTimeoutHandler(tcsCallTimeout, TimeUnit.MILLISECONDS)));
	}
}
