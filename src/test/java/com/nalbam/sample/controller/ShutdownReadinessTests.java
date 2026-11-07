package com.nalbam.sample.controller;

import com.nalbam.sample.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

// Proves that closing a real servlet web server context, as SIGTERM does, switches
// /ready to 503. The test starts and closes its own context on a random port, because
// the Spring test context cache cannot hand out a context that a test has closed.
class ShutdownReadinessTests {

	@Test
	void readyReturns503AfterContextClose() {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(DemoApplication.class)
				.properties("server.port=0")
				.run();
		try {
			assertInstanceOf(ServletWebServerApplicationContext.class, context);
			RestfulController controller = context.getBean(RestfulController.class);
			assertEquals(HttpStatus.OK, controller.ready().getStatusCode());

			context.close();
			assertFalse(context.isActive(), "context must be closed");

			ResponseEntity<Map<String, Object>> response = controller.ready();
			assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
			assertEquals(Map.of("ready", false), response.getBody());
			assertEquals(Map.of("live", true), controller.live());
		} finally {
			context.close();
		}
	}

}
