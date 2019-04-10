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

    @Value("${spring.application.name}")
    private String service;

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${server.port}")
    private Integer port;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 1000)
    public void call_spring() {
        if ("default".equals(profile)) {
            call("http://localhost:" + port + "/spring");
        } else {
            call("http://" + service + "-" + profile + "/spring");
        }
    }

    @Scheduled(fixedRate = 3000)
    public void call_dealy() {
        if ("default".equals(profile)) {
            call("http://localhost:" + port + "/dealy/1");
        } else {
            call("http://" + service + "-" + profile + "/dealy/1");
        }
    }

    @Scheduled(fixedRate = 325)
    public void call_node() {
        if ("default".equals(profile)) {
            return;
        }

        List<String> commands = Arrays.asList("/counter/up", "/counter/down", "/cache/node");

        Random random = new Random();

        call("http://sample-node-" + profile + commands.get(random.nextInt(commands.size())));
    }

    @Scheduled(fixedRate = 102)
    public void call_stress() {
        if ("default".equals(profile)) {
            return;
        }

        List<String> commands = Arrays.asList("sample-spring", "sample-node");

        Random random = new Random();

        call("http://" + commands.get(random.nextInt(commands.size())) + "-" + profile + "/stress");
    }

    private void call(String url) {
        log.info("req: {}", url);
        String res = restTemplate.getForObject(url, String.class);
        log.info("res: {}", res);
    }

}
