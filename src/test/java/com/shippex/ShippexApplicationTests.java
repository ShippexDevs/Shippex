package com.shippex;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShippexApplicationTests {

	@Test
	void applicationEnablesSpringBootAndAsyncExecution() {
		assertNotNull(ShippexApplication.class.getAnnotation(SpringBootApplication.class));
		assertNotNull(ShippexApplication.class.getAnnotation(EnableAsync.class));
	}

}
