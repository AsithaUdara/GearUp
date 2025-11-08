package com.gearup.chatbotservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebClient used to communicate with Ollama
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    
    private final OllamaProperties ollamaProperties;
    
    @Bean(name = "ollamaWebClient")
    public WebClient ollamaWebClient() {
        
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ollamaProperties.getTimeout())
            .responseTimeout(Duration.ofMillis(ollamaProperties.getTimeout()))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(ollamaProperties.getTimeout(), TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(ollamaProperties.getTimeout(), TimeUnit.MILLISECONDS)));
        
        return WebClient.builder()
            .baseUrl(ollamaProperties.getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
