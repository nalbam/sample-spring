package com.nalbam.sample.controller;

import com.nalbam.sample.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@Slf4j
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        String logo = "https://cdn.nalbam.com/logo/spring-boot.png";
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "Unknown";
            e.printStackTrace();
        }

        return "<h1>Hello Spring Boot!</h1><img src=" + logo + "></p><p>" + host + "</p>";
    }

    @GetMapping("/live")
    public String live() {
        log.info("live check");
        return "<h1>OK</h1><p>live</p>";
    }

    @GetMapping("/read")
    public String read() {
        log.info("read check");
        return "<h1>OK</h1><p>read</p>";
    }

    @GetMapping("/spring")
    public String spring() {
        log.info("spring check");
        return "<h1>OK</h1><p>spring</p>";
    }

    @GetMapping("/stress")
    public String stress() {
        log.info("stress check");

        float sum = 0;
        for (int i = 0; i < 1000000; i++) {
            sum += Math.sqrt(i);
        }

        return "<h1>OK</h1><p>" + sum + "</p>";
    }

    @GetMapping("/package")
    public Map<String, String> health() {
        return PackageUtil.getData(this.getClass());
    }

}
