package com.nalbam.sample.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class SendTask {

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 3245)
    public void call_sample_node() {
        List<String> commands = Arrays.asList("/counter/up", "/counter/down", "/cache/node");

        Random random = new Random();

        call("http://sample-node-" + profile + commands.get(random.nextInt(commands.size())));
    }

    @Scheduled(fixedRate = 2168)
    public void call_sample_spring() {
        call("http://sample-spring-" + profile + "/spring");
    }

    @Scheduled(fixedRate = 5312)
    public void call_sample_stress() {
        call("http://sample-spring-" + profile + "/stress");
    }

    @Scheduled(fixedRate = 3156)
    public void call_sample_dealy() {
        call("http://sample-spring-" + profile + "/dealy/1");
    }

    private void call(String url) {
        log.info("req: {}", url);
        String res = restTemplate.getForObject(url, String.class);
        log.info("res: {}", res);
    }

}
