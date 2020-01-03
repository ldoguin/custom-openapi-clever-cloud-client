package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.clevercloud.client.*;
import com.clevercloud.client.api.SelfApi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.*;

import java.text.DateFormat;
import java.util.TimeZone;


@SpringBootApplication
public class DemoApplication  {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(DemoApplication.class, args);
		ctx.close();
	}

	@Bean
	public CommandLineRunner run(WebClient cleverWebClient) {
		return (args) -> {
			DateFormat dateFormat = new RFC3339DateFormat();
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			ApiClient defaultClient = new ApiClient(cleverWebClient, new ObjectMapper(), dateFormat);

			SelfApi selfApiInstance = new SelfApi(defaultClient);
			System.out.println(selfApiInstance.getUser().block());
		};
	}
}
