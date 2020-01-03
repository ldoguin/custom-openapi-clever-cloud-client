package com.example.demo;

import com.example.demo.signposthack.WebClientOauthConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
public class WebClientOauth1FilterFunction implements ExchangeFilterFunction {

    @Autowired
    private WebClientOauthConsumer webClientOauthConsumer;

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        try {
            return next.exchange(webClientOauthConsumer.sign(request));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
