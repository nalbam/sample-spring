package com.nalbam.sample.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
// import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
// import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class SendTask {

    @Value("${spring.profiles.active}")
    private String profile;

    // @Value("${spring.application.name}")
    // private String service;

    @Value("${protocol}")
    private String protocol;

    @Value("${hostname}")
    private String hostname;

    @Value("${fault.rate}")
    private Integer faultRate;

    // @Value("${task.scheduled}")
    private Boolean scheduled = true;

    private final RestTemplate restTemplate;

    public SendTask(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Scheduled(fixedRate = 320)
    public void stress() {
        if (!scheduled) {
            return;
        }

        String url = getHostname() + "/stress";

        String res = restTemplate.getForObject(url, String.class);

        log.info("task /stress {}", res);
    }

    @Scheduled(fixedRate = 330)
    public void loop() {
        if (!scheduled) {
            return;
        }

        Integer count = 5;

        String url = getHostname() + "/loop/" + count;

        String res = restTemplate.getForObject(url, String.class);

        log.info("task /loop {}", res);
    }

    @Scheduled(fixedRate = 340)
    public void redis() {
        if (!scheduled) {
            return;
        }

        List<String> commands = Arrays.asList("/counter/up", "/counter/down", "/cache/node");

        String url = getHostname() + commands.get((new Random()).nextInt(commands.size()));

        String res = restTemplate.getForObject(url, String.class);

        log.info("task /redis {}", res);
    }

    @Scheduled(fixedRate = 350)
    public void fault() {
        if (!scheduled) {
            return;
        }

        String url = getHostname() + "/fault/" + faultRate;

        String res = restTemplate.getForObject(url, String.class);

        log.info("task /fault {}", res);
    }

    // @Scheduled(fixedRate = 360)
    public void delay() {
        if (!scheduled) {
            return;
        }

        Integer delay = (new Random()).nextInt(5) + 1;

        String url = getHostname() + "/delay/" + delay;

        String res = restTemplate.getForObject(url, String.class);

        log.info("task /delay {}", res);
    }

    // @Scheduled(fixedRate = 370)
    public void bookinfo() {
        if (!scheduled) {
            return;
        }
        // if ("default".equals(profile)) {
        //     req("http://productpage.default.svc.cluster.local:9080/productpage");
        // } else {
        //     req(protocol + "://bookinfo." + hostname + "/productpage");
        // }
        // req("http://productpage.default.svc.cluster.local:9080/productpage");
    }

    private String getHostname() {
        if ("default".equals(profile)) {
            return "http://sample-node";
        } else {
            return protocol + "://sample-node." + hostname;
        }
    }

    // @Async
    // private CompletableFuture<String> req(String url) {
    //     log.info("req: {}", url);
    //     String res = restTemplate.getForObject(url, String.class);
    //     log.info("res: {}", res);
    //     return CompletableFuture.completedFuture(res);
    // }

}
