package com.example.demo;

import com.example.demo.signposthack.WebClientOauthConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CleverCloudConf {

    @Value("${clevercloud.consumer}")
    private String consumerKey;

    @Value("${clevercloud.consumerSecret}")
    private String consumerSecret;

    @Value("${clevercloud.token}")
    private String token;

    @Value("${clevercloud.tokenSecret}")
    private String tokenSecret;

    @Bean
    public WebClientOauthConsumer webClientOauthConsumer(){
        WebClientOauthConsumer consumer = new WebClientOauthConsumer(
                consumerKey,
                consumerSecret);
        consumer.setTokenWithSecret(token, tokenSecret);
        return consumer;
    }

    @Bean
    public WebClient cleverWebClient(WebClientOauth1FilterFunction webClientOauth1Filter){
        return WebClient.builder().filter(webClientOauth1Filter).build();
    }


}
