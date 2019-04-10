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

    @Value("${namespace}")
    private String namespace;

    @Value("${spring.application.name}")
    private String service;

    @Value("${server.port}")
    private Integer port;

    private final RestTemplate restTemplate;

    public SendTask(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Scheduled(fixedRate = 1000)
    public void spring() {
        if ("default".equals(namespace)) {
            call("http://localhost:" + port + "/spring");
        } else {
            call("http://" + service + "-" + namespace + "/spring");
        }
    }

    @Scheduled(fixedRate = 10000)
    public void dealy() {
        if ("default".equals(namespace)) {
            call("http://localhost:" + port + "/dealy/3");
        } else {
            call("http://" + service + "-" + namespace + "/dealy/3");
        }
    }

    @Scheduled(fixedRate = 567)
    public void node() {
        if ("default".equals(namespace)) {
            return;
        }

        List<String> commands = Arrays.asList("/counter/up", "/counter/down", "/cache/node");

        Random random = new Random();

        call("http://sample-node-" + namespace + commands.get(random.nextInt(commands.size())));
    }

    @Scheduled(fixedRate = 234)
    public void stress() {
        if ("default".equals(namespace)) {
            return;
        }

        List<String> commands = Arrays.asList("sample-spring", "sample-node");

        Random random = new Random();

        call("http://" + commands.get(random.nextInt(commands.size())) + "-" + namespace + "/stress");
    }

    @Async
    private CompletableFuture<String> call(String url) {
        log.info("req: {}", url);

        String res = restTemplate.getForObject(url, String.class);

        log.info("res: {}", res);

        return CompletableFuture.completedFuture(res);
    }

}
