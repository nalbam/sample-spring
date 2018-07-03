package com.nalbam.sample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
@Controller
public class HomeController {

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

        model.put("host", host);
        model.put("date", sdf.format(new Date()));

        return "index";
    }

}
