package com.nalbam.sample.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "<p>OK</p>";
    }

//    @GetMapping("/health")
//    public Map<String, String> health() {
//        return PackageUtil.getData(this.getClass());
//    }

}
