package com.shippex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ShippexApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShippexApplication.class, args);
	}

}
