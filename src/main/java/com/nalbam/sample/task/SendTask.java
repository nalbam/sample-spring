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

    private final RestTemplate restTemplate;

    public SendTask(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Scheduled(fixedRate = 60000)
    public void pulse() {
        if ("test".equals(profile)) {
            return;
        }

        log.debug("bit");
    }

    @Scheduled(fixedRate = 890)
    public void dealy() {
        if ("test".equals(profile)) {
            return;
        }

        Integer dealy = (new Random()).nextInt(5);

        if ("default".equals(profile)) {
            call("http://localhost:" + port + "/dealy/" + dealy);
        } else {
            call("http://" + service + "/dealy/" + dealy);
        }
    }

    @Scheduled(fixedRate = 789)
    public void fault() {
        if ("test".equals(profile)) {
            return;
        }

        Integer fault = 5;

        if ("default".equals(profile)) {
            call("http://localhost:" + port + "/fault/" + fault);
        } else {
            call("http://" + service + "/fault/" + fault);
        }
    }

    @Scheduled(fixedRate = 1234)
    public void bookinfo() {
        if ("default".equals(profile) || "test".equals(profile)) {
            return;
        }

        call("http://productpage.default.svc.cluster.local:9080/productpage");
    }

    @Scheduled(fixedRate = 456)
    public void node() {
        if ("default".equals(profile) || "test".equals(profile)) {
            return;
        }

        List<String> commands = Arrays.asList("/counter/up", "/counter/down", "/cache/node");

        Random random = new Random();

        call("http://sample-node" + commands.get(random.nextInt(commands.size())));
    }

    @Scheduled(fixedRate = 567)
    public void stress() {
        if ("default".equals(profile) || "test".equals(profile)) {
            return;
        }

        List<String> commands = Arrays.asList("sample-spring", "sample-node");

        Random random = new Random();

        call("http://" + commands.get(random.nextInt(commands.size())) + "/stress");
    }

    @Scheduled(fixedRate = 678)
    public void spring() {
        if ("default".equals(profile) || "test".equals(profile)) {
            return;
        }

        List<String> commands = Arrays.asList("sample-spring", "sample-node");

        Random random = new Random();

        call("http://" + commands.get(random.nextInt(commands.size())) + "/spring");
    }

    @Async
    private CompletableFuture<String> call(String url) {
        log.debug("req: {}", url);

        String res = restTemplate.getForObject(url, String.class);

        log.debug("res: {}", res);

        return CompletableFuture.completedFuture(res);
    }

}
