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

    @Value("${namespace}")
    private String namespace;

    @Value("${spring.application.name}")
    private String service;

    @Value("${server.port}")
    private Integer port;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 1000)
    public void call_spring() {
        if ("default".equals(namespace)) {
            call("http://localhost:" + port + "/spring");
        } else {
            call("http://" + service + "-" + namespace + "/spring");
        }
    }

    @Scheduled(fixedRate = 3000)
    public void call_dealy() {
        if ("default".equals(namespace)) {
            call("http://localhost:" + port + "/dealy/1");
        } else {
            call("http://" + service + "-" + namespace + "/dealy/1");
        }
    }

    @Scheduled(fixedRate = 325)
    public void call_node() {
        if ("default".equals(namespace)) {
            return;
        }

        List<String> commands = Arrays.asList("/counter/up", "/counter/down", "/cache/node");

        Random random = new Random();

        call("http://sample-node-" + namespace + commands.get(random.nextInt(commands.size())));
    }

    @Scheduled(fixedRate = 102)
    public void call_stress() {
        if ("default".equals(namespace)) {
            return;
        }

        List<String> commands = Arrays.asList("sample-spring", "sample-node");

        Random random = new Random();

        call("http://" + commands.get(random.nextInt(commands.size())) + "-" + namespace + "/stress");
    }

    private void call(String url) {
        log.info("req: {}", url);
        String res = restTemplate.getForObject(url, String.class);
        log.info("res: {}", res);
    }

}
