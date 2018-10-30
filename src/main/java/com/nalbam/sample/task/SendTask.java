package com.nalbam.sample.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class SendTask {

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 3000)
    public void call_sample_node() {
        call("http://sample-node-" + profile + "/counter/up");
    }

    @Scheduled(fixedRate = 2000)
    public void call_sample_spring() {
        call("http://sample-spring-" + profile + "/spring");
    }

    @Scheduled(fixedRate = 5000)
    public void call_sample_stress() {
        call("http://sample-spring-" + profile + "/stress");
    }

    @Scheduled(fixedRate = 3000)
    public void call_sample_dealy() {
        call("http://sample-spring-" + profile + "/dealy/1");
    }

    private void call(String url) {
        String res = restTemplate.getForObject(url, String.class);
        log.info("Receive : {}", res);
    }

}
