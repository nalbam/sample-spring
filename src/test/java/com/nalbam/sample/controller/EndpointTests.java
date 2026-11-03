package com.nalbam.sample.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.lang.management.ManagementFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EndpointTests {

	@Autowired
	private MockMvc mockMvc;

	@Value("${version}")
	private String version;

	@Test
	void live() throws Exception {
		mockMvc.perform(get("/live"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.result").value("OK"))
				.andExpect(jsonPath("$.type").value("live"));
	}

	@Test
	void health() throws Exception {
		// Make sure the JVM has run for at least one second, so an uptime counted from
		// the request (always 0) cannot pass the lower bound below.
		long jvmUptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
		if (jvmUptimeMs < 1100) {
			Thread.sleep(1100 - jvmUptimeMs);
		}

		long beforeSec = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
		MvcResult result = mockMvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.result").value("ok"))
				.andExpect(jsonPath("$.type").value("health"))
				.andExpect(jsonPath("$.version").value(version))
				.andExpect(jsonPath("$.artifactId").isString())
				.andReturn();
		long afterSec = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

		JsonNode body = new JsonMapper().readTree(result.getResponse().getContentAsString());
		assertTrue(body.get("result").isString(), "result must be a string");
		assertTrue(body.get("version").isString(), "version must be a string");

		JsonNode uptime = body.get("uptime");
		assertTrue(uptime != null && uptime.isIntegralNumber(), "uptime must be an integer: " + uptime);
		long uptimeSec = uptime.asLong();
		assertTrue(uptimeSec >= 1, "uptime must count from JVM start: " + uptimeSec);
		assertTrue(beforeSec <= uptimeSec && uptimeSec <= afterSec,
				"uptime " + uptimeSec + " must be JVM uptime in [" + beforeSec + ", " + afterSec + "]");
	}

	@Test
	void index() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"));
	}

	@Test
	void drop() throws Exception {
		mockMvc.perform(get("/drop/10"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"));
	}

	@Test
	void loopZeroReturnsWithoutCallingNode() throws Exception {
		mockMvc.perform(get("/loop/0"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("OK"));
	}

	@Test
	void actuatorHealth() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

}
