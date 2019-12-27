package com.nalbam.sample.controller;

import com.nalbam.sample.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
public class RestfulController {

    @GetMapping("/live")
    public Map<String, Object> live() {
        log.info("live check");

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "live");

        return map;
    }

    @GetMapping("/read")
    public Map<String, Object> read() {
        log.info("read check");

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "live");

        return map;
    }

    @GetMapping("/spring")
    public Map<String, Object> spring() {
        log.info("spring");

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "spring");

        return map;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        log.info("health");

        Map<String, Object> map = PackageUtil.getData(this.getClass());
        map.put("result", "OK");
        map.put("type", "health");

        return map;
    }

    @GetMapping("/stress")
    public Map<String, Object> stress() {
        log.info("stress check");

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

    @GetMapping("/dealy/{sec}")
    public Map<String, Object> dealy(@PathVariable Integer sec) {
        log.info("dealy {}", sec);

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
        log.info("timeout {}", sec);

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
    public Map<String, Object> fault(@PathVariable Integer rate) {
        log.info("fault {}", rate);

        Random random = new Random();

        try {
            Thread.sleep(random.nextInt(500) + 100);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, Object> map = PackageUtil.getData(this.getClass());
        map.put("result", "OK");
        map.put("type", "fault");

        return map;
    }

}
