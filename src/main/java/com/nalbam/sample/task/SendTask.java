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

    @Value("${server.port}")
    private Integer port;

    // @Value("${task.scheduled}")
    private Boolean scheduled = true;

    private List<String> servers = Arrays.asList("sample-spring:8080", "sample-node:3000");

    private final RestTemplate restTemplate;

    public SendTask(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Scheduled(fixedRate = 234)
    public void loop() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }

        Integer count = 5;

        call("http://" + servers.get((new Random()).nextInt(servers.size())) + "/loop/" + count);
    }

    @Scheduled(fixedRate = 345)
    public void stress() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }

        call("http://" + servers.get((new Random()).nextInt(servers.size())) + "/stress");
    }

    @Scheduled(fixedRate = 456)
    public void redis() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }

        List<String> commands = Arrays.asList("/counter/up", "/counter/down", "/cache/node");

        call("http://" + servers.get((new Random()).nextInt(servers.size())) + commands.get((new Random()).nextInt(commands.size())));
    }

    @Scheduled(fixedRate = 567)
    public void delay() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }

        Integer delay = (new Random()).nextInt(5) + 1;

        call("http://" + servers.get((new Random()).nextInt(servers.size())) + "/delay/" + delay);
    }

    @Scheduled(fixedRate = 678)
    public void fault() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }

        Integer fault = 5;

        call("http://" + servers.get((new Random()).nextInt(servers.size())) + "/fault/" + fault);
    }

    // @Scheduled(fixedRate = 789)
    public void bookinfo() {
        if (!scheduled || "default".equals(profile) || "test".equals(profile)) {
            return;
        }

        call("http://productpage.default.svc.cluster.local:9080/productpage");
    }

    @Async
    private CompletableFuture<String> call(String url) {
        log.debug("req: {}", url);

        String res = restTemplate.getForObject(url, String.class);

        log.debug("res: {}", res);

        return CompletableFuture.completedFuture(res);
    }

}
