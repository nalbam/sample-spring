package com.nalbam.sample.controller;

import com.nalbam.sample.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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
        log.info("spring check");

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");
        map.put("type", "spring");

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

    @GetMapping("/package")
    public Map<String, Object> health() {
        return PackageUtil.getData(this.getClass());
    }

}
