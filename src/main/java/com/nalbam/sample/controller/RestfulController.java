package com.nalbam.sample.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nalbam.sample.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

@Slf4j
@RestController
public class RestfulController {

    @Value("${spring.profiles.active}")
    private String profile;

    private final RestTemplate restTemplate;

    public RestfulController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping("/live")
    public Map<String, Object> live() {
        log.debug("live");

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "live");

        return map;
    }

    @GetMapping("/read")
    public Map<String, Object> read() {
        log.debug("read");

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "live");

        return map;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        log.debug("health");

        Map<String, Object> map = PackageUtil.getData(this.getClass());
        map.put("result", "OK");
        map.put("type", "health");

        return map;
    }

    @GetMapping("/stress")
    public Map<String, Object> stress() {
        log.debug("stress");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        Double sum = 0d;
        for (int i = 0; i < 1000000; i++) {
            sum += Math.sqrt(i);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "stress");
        map.put("sum", sum);
        map.put("date", sdf.format(new Date()));

        return map;
    }

    @GetMapping("/node")
    public String node() {
        log.debug("node");

        String url;

        if ("default".equals(profile)) {
            url = "http://localhost:3000/spring";
        } else {
            url = "http://sample-node/spring";
        }

        String res = restTemplate.getForObject(url, String.class);

        return res;
    }

    @GetMapping("/loop/{count}")
    public Map<String, Object> loop(@PathVariable Integer count) {
        log.debug("loop {}", count);

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");

        if (count <= 0) {
            return map;
        }

        count--;

        String url;

        if ("default".equals(profile)) {
            url = "http://localhost:8080/loop/" + count;
        } else {
            url = "http://sample-spring/loop/" + count;
        }

        String json = restTemplate.getForObject(url, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> res = null;

        try {
            res = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.info("Exception converting {} to map", json, e);
        }

        map.put("data", res);

        return map;
    }

    @GetMapping("/dealy/{sec}")
    public Map<String, Object> dealy(@PathVariable Integer sec) {
        log.debug("dealy {}", sec);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        if (sec > 0) {
            try {
                Thread.sleep(sec * 1000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "dealy");
        map.put("date", sdf.format(new Date()));

        return map;
    }

    @GetMapping("/timeout/{sec}")
    public Map<String, Object> timeout(@PathVariable Integer sec) throws TimeoutException {
        log.debug("timeout {}", sec);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        try {
            Thread.sleep(sec * 1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        throw new TimeoutException("Timeout");
    }

    @GetMapping("/fault/{rate}")
    public Map<String, Object> fault(@PathVariable Integer rate) throws RuntimeException {
        log.debug("fault {}", rate);

        Integer random = (new Random()).nextInt(100);

        if (random < rate) {
            throw new RuntimeException("Fault! " + random);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "fault");

        return map;
    }

}
