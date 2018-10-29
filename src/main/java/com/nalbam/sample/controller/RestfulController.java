package com.nalbam.sample.controller;

import com.nalbam.sample.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import com.nalbam.sample.service.AmazonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@RestController
public class RestfulController {

    @Autowired
    private AmazonService amazonService;

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

    @GetMapping("/package")
    public Map<String, Object> health() {
        return PackageUtil.getData(this.getClass());
    }

    @GetMapping("/spring")
    public Map<String, Object> spring() {
        log.info("spring check");

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "spring");

        return map;
    }

    @GetMapping("/buckets")
    public Map<String, Object> buckets() {
        log.info("aws s3 buckets");

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("buckets", amazonService.listBuckets());

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
        map.put("sum", sum);
        map.put("date", sdf.format(new Date()));

        return map;
    }

    @GetMapping("/sleep/{sec}")
    public Map<String, Object> slow(@PathVariable Integer sec) {
        log.info("stress check");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        try {
            Thread.sleep(sec * 1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("date", sdf.format(new Date()));

        return map;
    }

}
