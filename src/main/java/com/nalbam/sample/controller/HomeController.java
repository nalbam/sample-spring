package com.nalbam.sample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Controller
public class HomeController {

  @Value("${cluster}")
  private String cluster;

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

        model.put("host", host);
        model.put("date", sdf.format(new Date()));
        model.put("cluster", cluster);
        model.put("profile", profile);
        model.put("message", message);
        model.put("version", version);

        log.debug("index {}", model);

        return "index";
    }

    @GetMapping("/drop/{rate}")
    public String drop(Map<String, Object> model, @PathVariable Integer rate) {
        // rate
        model.put("rate", rate);

        log.debug("drop {}", model);

        return "drop";
    }

    @GetMapping("/re/billing")
    public RedirectView billing(Map<String, Object> model) {
        return new RedirectView("/billing/transaction");
    }

    @GetMapping("/re/google")
    public String google(Map<String, Object> model) {
        return "redirect:http://www.google.com";
    }

}
