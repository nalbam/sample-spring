package com.nalbam.sample.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EndpointTests {

	@Autowired
	private MockMvc mockMvc;

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
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("OK"))
				.andExpect(jsonPath("$.type").value("health"))
				.andExpect(jsonPath("$.version").exists());
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
