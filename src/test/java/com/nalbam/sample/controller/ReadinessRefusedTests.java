package com.nalbam.sample.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Changes the readiness state of its context, so the context is not reused by other tests.
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class ReadinessRefusedTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ApplicationContext context;

	@Test
	void readyReturns503AndLiveStaysUpWhenRefusingTraffic() throws Exception {
		mockMvc.perform(get("/ready"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ready").value(true));

		AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC);

		mockMvc.perform(get("/ready"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.ready").value(false));
		mockMvc.perform(get("/live"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.live").value(true));
	}

}
