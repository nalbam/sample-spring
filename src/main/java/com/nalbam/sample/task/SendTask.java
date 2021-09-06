package com.nalbam.sample.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class SendTask {

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${spring.application.name}")
    private String service;

    @Value("${protocol}")
    private String protocol;

    @Value("${hostname}")
    private String hostname;

    @Value("${server.port}")
    private Integer port;

    // @Value("${task.scheduled}")
    private Boolean scheduled = true;

    private final RestTemplate restTemplate;

    public SendTask(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Scheduled(fixedRate = 32)
    public void stress() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }
        call(getHostname() + "/stress");
    }

    @Scheduled(fixedRate = 33)
    public void loop() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }
        Integer count = 5;
        call(getHostname() + "/loop/" + count);
    }

    @Scheduled(fixedRate = 34)
    public void redis() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }
        List<String> commands = Arrays.asList("/counter/up", "/counter/down", "/cache/node");
        call(getHostname() + commands.get((new Random()).nextInt(commands.size())));
    }

    @Scheduled(fixedRate = 35)
    public void fault() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }
        Integer fault = 5;
        call(getHostname() + "/fault/" + fault);
    }

    @Scheduled(fixedRate = 36)
    public void delay() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }
        Integer delay = (new Random()).nextInt(5) + 1;
        call(getHostname() + "/delay/" + delay);
    }

    @Scheduled(fixedRate = 37)
    public void bookinfo() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }
        call(protocol + "://bookinfo." + hostname + "/productpage");
    }

    private String getHostname() {
        if ("default".equals(profile)) {
            return "http://sample-node:3000";
        } else {
            return protocol + "://sample-node." + hostname;
        }
    }

    @Async
    private CompletableFuture<String> call(String url) {
        log.info("req: {}", url);
        String res = restTemplate.getForObject(url, String.class);
        log.info("res: {}", res);
        return CompletableFuture.completedFuture(res);
    }

}
