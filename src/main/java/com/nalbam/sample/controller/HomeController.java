package com.nalbam.sample.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

@Controller
public class HomeController {

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${message}")
    private String message;

    @Value("${version}")
    private String version;

    @GetMapping("/")
    public String index(Map<String, Object> model) {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "Unknown";
            e.printStackTrace();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        // profile
        model.put("profile", profile);

        // message
        model.put("message", message);

        // version
        model.put("version", version);

        // host
        model.put("host", host);

        // date
        model.put("date", sdf.format(new Date()));

        return "index";
    }

}
